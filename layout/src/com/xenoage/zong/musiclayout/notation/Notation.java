package com.xenoage.zong.musiclayout.notation;

import com.xenoage.zong.core.music.MusicElement;
import com.xenoage.zong.musiclayout.spacing.ElementWidth;

/**
 * This is the interface for {@link MusicElement} layout
 * information classes.
 * 
 * It is called <code>Notation</code>, because it contains the
 * positioned representation of the by then
 * ambiguous raw musical data.
 * 
 * It contains the width of an element
 * and additional layout information.
 * 
 * For example, the class {@link ChordNotation} contains the
 * alignment of its notes and accidentals.
 *
 * @author Andreas Wenger
 */
public interface Notation {

	public MusicElement getElement();

	public ElementWidth getWidth();

}
