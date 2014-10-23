package musicxmltestsuite.tests.base;

import static com.xenoage.utils.collections.CollectionUtils.alist;
import static com.xenoage.utils.math.Fraction.fr;
import static com.xenoage.zong.core.music.Pitch.pi;
import static com.xenoage.zong.core.music.beam.Beam.beamFromChords;
import static musicxmltestsuite.tests.utils.Utils.ch;
import static musicxmltestsuite.tests.utils.Utils.gr;

import com.xenoage.zong.commands.core.music.beam.BeamAdd;
import com.xenoage.zong.core.music.chord.Chord;

/**
 * A grace note that appears at the measure end (without any steal-from-* attribute set).
 * Some applications need to convert this into an after-grace. 
 * 
 * @author Andreas Wenger
 */
public interface Base24c
	extends Base {

	@Override default String getFileName() {
		return "24c-GraceNote-MeasureEnd.xml";
	}
	
	default Chord[] getExpectedChords() {
		Chord[] ret = {
			ch(fr(2, 4), pi('E', 0, 5)),
			ch(fr(2, 4), pi('E', 0, 5)),
			gr(fr(1, 16), false, pi('G', 0, 5)),
			gr(fr(1, 16), false, pi('A', 0, 5))};
		new BeamAdd(beamFromChords(alist(ret[2], ret[3]))).execute();
		return ret;
	}

}
