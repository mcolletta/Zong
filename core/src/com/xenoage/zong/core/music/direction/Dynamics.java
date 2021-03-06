package com.xenoage.zong.core.music.direction;

import com.xenoage.zong.core.music.MusicElementType;
import com.xenoage.zong.core.position.MP;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * Class for a dynamics sign like forte, piano, sforzando and so on.
 *
 * @author Andreas Wenger
 */
@Data @EqualsAndHashCode(callSuper=false)
public final class Dynamics
	extends Direction {

	private final DynamicsType type;

	@Override public MusicElementType getMusicElementType() {
		return MusicElementType.Dynamics;
	}
	
	@Override public MP getMP() {
		return MP.getMPFromParent(this);
	}
	
}
