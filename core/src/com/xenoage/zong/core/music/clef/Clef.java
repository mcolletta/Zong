package com.xenoage.zong.core.music.clef;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.xenoage.utils.annotations.NonNull;
import com.xenoage.zong.core.music.MeasureElement;
import com.xenoage.zong.core.music.MusicElementType;
import com.xenoage.zong.core.position.MP;
import com.xenoage.zong.core.position.MPContainer;


/**
 * Class for a clef.
 * 
 * @author Andreas Wenger
 */
@Data @EqualsAndHashCode(exclude="parent")
public final class Clef
	implements MeasureElement
{

	/** The type of the clef. */
	@lombok.NonNull
	@NonNull private ClefType type;
	
	/** Back reference: the parent element, or null, if not part of a score. */
	private MPContainer parent;

	@Override public MusicElementType getMusicElementType() {
		return MusicElementType.Clef;
	}
	
	@Override public MP getMP() {
		return MP.getMPFromParent(this);
	}
	
}
