package com.xenoage.zong.musiclayout.layouter.verticalframefilling;

import com.xenoage.zong.core.Score;
import com.xenoage.zong.musiclayout.spacing.FrameSpacing;

/**
 * This vertical frame filling strategy changes nothing.
 * It simply returns the given frame arrangement.
 * 
 * @author Andreas Wenger
 */
public class NoVerticalFrameFillingStrategy
	implements VerticalFrameFillingStrategy {

	public static final NoVerticalFrameFillingStrategy instance = new NoVerticalFrameFillingStrategy();


	@Override public FrameSpacing computeFrameArrangement(FrameSpacing frameArr, Score score) {
		return frameArr;
	}

}
