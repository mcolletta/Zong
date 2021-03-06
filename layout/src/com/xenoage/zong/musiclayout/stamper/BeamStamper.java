package com.xenoage.zong.musiclayout.stamper;

import static com.xenoage.utils.collections.CollectionUtils.alist;
import static com.xenoage.utils.kernel.Range.range;
import static com.xenoage.utils.math.MathUtils.interpolateLinear;
import static com.xenoage.zong.core.music.format.SP.sp;
import static com.xenoage.zong.musiclayout.notation.BeamNotation.hookLengthIs;
import static com.xenoage.zong.musiclayout.notation.BeamNotation.lineHeightIs;
import static com.xenoage.zong.musiclayout.notation.beam.Fragment.HookLeft;
import static com.xenoage.zong.musiclayout.notation.beam.Fragment.HookRight;
import static com.xenoage.zong.musiclayout.notation.beam.Fragment.Start;
import static com.xenoage.zong.musiclayout.notation.beam.Fragment.Stop;

import java.util.List;

import com.xenoage.zong.core.music.chord.StemDirection;
import com.xenoage.zong.core.music.format.SP;
import com.xenoage.zong.musiclayout.notation.beam.Fragment;
import com.xenoage.zong.musiclayout.notation.beam.Fragments;
import com.xenoage.zong.musiclayout.spacing.BeamSpacing;
import com.xenoage.zong.musiclayout.stampings.BeamStamping;
import com.xenoage.zong.musiclayout.stampings.StaffStamping;

/**
 * This strategy creates the stampings for a beam.
 * 
 * TODO: Correct handling for beams containing different stem
 * directions. E.g. hooks must always be placed on the inner side (note side).
 * 
 * @author Andreas Wenger
 */
public class BeamStamper {

	public static final BeamStamper beamStamper = new BeamStamper();
	

	/**
	 * Computes the stampings for the given beam and returns them.
	 * @param beam        the beam to stamp
	 * @param leftStaff   the staff stamping, where the first chord of the beam is stamped on
	 * @param rightStaff  the staff stamping, where the last chord of the beam is stamped on
	 */
	public List<BeamStamping> stamp(BeamSpacing beam, StaffStamping leftStaff,
		StaffStamping rightStaff) {
		
		int beamSize = beam.notation.element.size();
		SP leftEndSp = beam.getStemEndSp(0);
		SP rightEndSp = beam.getStemEndSp(beamSize - 1);
		StemDirection leftDir = beam.getStemDirection(0);
		StemDirection rightDir = beam.getStemDirection(beamSize - 1);
		
		//number of beam lines
		int linesCount = beam.notation.getLinesCount();
		List<BeamStamping> ret = alist(linesCount);
		
		//first line (8th line) is always continuous
		float leftLp = leftEndSp.lp;
		float rightLp = rightEndSp.lp;
		BeamStamping beam8th = new BeamStamping(beam, leftStaff, rightStaff,
			leftEndSp.withLp(leftLp), rightEndSp.withLp(rightLp), leftDir);
		ret.add(beam8th);
		
		//the next lines can be broken, if there are different rhythms or beam subdivisions
		//this is stored in the notation
		float leftXMm = leftEndSp.xMm;
		float rightXMm = rightEndSp.xMm;
		for (int i : range(beam.notation.linesFragments)) {
			int line = i + 1;
			float lineLp = -1 * leftDir.getSign() * (lineHeightIs + beam.notation.gapIs) * 2 * line;
			float leftLineLp = leftLp + lineLp;
			float rightLineLp = rightLp + lineLp;
			//create the line stampings
			float startX = 0;
			Fragments fragments = beam.notation.linesFragments.get(i);
			for (int iChord : range(fragments)) {
				Fragment fragment = fragments.get(iChord);
				float stemX = beam.getStemEndSp(iChord).xMm;
				if (fragment == Start) {
					//begin a new beam line
					startX = stemX;
				}
				else if (fragment == Stop) {
					//end the beam line and stem it
					float stopX = stemX;
					SP leftSp = sp(startX,
						interpolateLinear(leftLineLp, rightLineLp, leftXMm, rightXMm, startX));
					SP rightSp = sp(stopX,
						interpolateLinear(leftLineLp, rightLineLp, leftXMm, rightXMm, stopX));
					BeamStamping stamping = new BeamStamping(beam, leftStaff, rightStaff,
						leftSp, rightSp, leftDir);
					ret.add(stamping);
				}
				else if (fragment == HookLeft || fragment == HookRight) {
					//left hook
					float length = hookLengthIs * leftStaff.is;
					float x1 = (fragment == HookLeft ? stemX - length : stemX);
					float x2 = (fragment == HookLeft ? stemX : stemX + length);
					SP leftSp = sp(x1, interpolateLinear(leftLineLp, rightLineLp, leftXMm, rightXMm, x1));
					SP rightSp = sp(x2, interpolateLinear(leftLineLp, rightLineLp, leftXMm, rightXMm, x2));
					BeamStamping stamping = new BeamStamping(beam, leftStaff, rightStaff,
						leftSp, rightSp, leftDir);
					ret.add(stamping);
				}
			}
		}

		return ret;
	}

}
