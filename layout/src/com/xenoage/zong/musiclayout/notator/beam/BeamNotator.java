package com.xenoage.zong.musiclayout.notator.beam;

import static com.xenoage.zong.core.music.chord.StemDirection.Down;
import static com.xenoage.zong.core.music.chord.StemDirection.Up;
import static com.xenoage.zong.musiclayout.notator.beam.BeamFragmenter.beamFragmenter;

import java.util.List;

import com.xenoage.zong.core.music.beam.Beam;
import com.xenoage.zong.core.music.chord.StemDirection;
import com.xenoage.zong.core.position.MPElement;
import com.xenoage.zong.musiclayout.layouter.Context;
import com.xenoage.zong.musiclayout.notation.BeamNotation;
import com.xenoage.zong.musiclayout.notation.ChordNotation;
import com.xenoage.zong.musiclayout.notation.Notations;
import com.xenoage.zong.musiclayout.notation.beam.Fragments;
import com.xenoage.zong.musiclayout.notation.chord.StemNotation;
import com.xenoage.zong.musiclayout.notator.ElementNotator;
import com.xenoage.zong.musiclayout.notator.beam.lines.BeamRules;

/**
 * Computes {@link BeamNotation}s and modifies the {@link StemNotation}s of a {@link Beam}.
 * 
 * @author Andreas Wenger
 */
public class BeamNotator
	implements ElementNotator {
	
	public static final BeamNotator beamNotator = new BeamNotator();
	
	
	@Override public BeamNotation compute(MPElement element, Context context, Notations notations) {
		return compute((Beam) element, notations);
	}

	
	public BeamNotation compute(Beam beam, Notations notations) {
		//compute fragments
		List<Fragments> fragments = beamFragmenter.compute(beam);
		//compute stem length and gap
		BeamRules beamRules = BeamRules.getRules(beam);
		float gapIs = beamRules.getGapIs();
		//collect chords
		List<ChordNotation> chords = notations.getBeamChords(beam);
		//create notation
		BeamNotation beamNotation = new BeamNotation(beam, fragments, gapIs, chords);
		return beamNotation;
	}

}
