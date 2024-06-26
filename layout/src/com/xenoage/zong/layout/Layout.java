package com.xenoage.zong.layout;

import static com.xenoage.utils.collections.CList.clist;
import static com.xenoage.utils.collections.CollectionUtils.alist;
import static com.xenoage.utils.kernel.Range.range;
import static com.xenoage.zong.layout.LP.lp;

import java.util.ArrayList;
import java.util.List;

import com.xenoage.utils.annotations.Untested;
import com.xenoage.utils.collections.CList;
import com.xenoage.utils.math.geom.Point2f;
import com.xenoage.zong.core.Score;
import com.xenoage.zong.core.format.LayoutFormat;
import com.xenoage.zong.core.position.MP;
import com.xenoage.zong.layout.frames.FP;
import com.xenoage.zong.layout.frames.Frame;
import com.xenoage.zong.layout.frames.GroupFrame;
import com.xenoage.zong.layout.frames.ScoreFrame;
import com.xenoage.zong.layout.frames.ScoreFrameChain;
import com.xenoage.zong.musiclayout.ScoreLP;
import com.xenoage.zong.musiclayout.ScoreLayout;
import com.xenoage.zong.musiclayout.layouter.Context;
import com.xenoage.zong.musiclayout.layouter.ScoreLayoutArea;
import com.xenoage.zong.musiclayout.layouter.ScoreLayouter;
import com.xenoage.zong.musiclayout.layouter.Target;
import com.xenoage.zong.musiclayout.settings.LayoutSettings;
import com.xenoage.zong.symbols.SymbolPool;
import com.xenoage.zong.util.event.ScoreChangedEvent;

//-----------------------------------------------------------
import com.xenoage.zong.musiclayout.ScoreFrameLayout;
import java.util.HashMap;
import com.xenoage.utils.kernel.Tuple2;
import com.xenoage.utils.pdlib.PMap;
import static com.xenoage.utils.pdlib.PMap.pmap;
import com.xenoage.utils.math.geom.Rectangle2f;
//-----------------------------------------------------------

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Class for the layout of a score document.
 * 
 * It consists of several pages, each containing several frames.
 * 
 * @author Andreas Wenger
 * @author Uli Teschemacher
 */
@Data @AllArgsConstructor public class Layout {

	/** Default settings */
	private LayoutDefaults defaults;

	/** The list of pages */
	private ArrayList<Page> pages;

	/** The list of selected frames */
	private ArrayList<Frame> selectedFrames;

	//-----------------------------------------------------------
	/** The chains of score frames */
	public final PMap<Score, ScoreFrameChain> scoreFrameChains;
	
	/** The musical layouts */
	public final PMap<ScoreFrameChain, ScoreLayout> scoreLayouts;

	//cache of parent frames and pages for fast lookup
	HashMap<Frame, Page> parentPages = null;
	HashMap<Frame, GroupFrame> parentFrames = null;
	//-----------------------------------------------------------

	/**
	 * Initializes an empty {@link Layout}.
	 */
	public Layout(LayoutDefaults defaults) {
		this.defaults = defaults;
		this.pages = alist();
		this.selectedFrames = alist();
		//--------------------------------
		this.scoreFrameChains = pmap();
		this.scoreLayouts = pmap();
	}

	/**
	 * Adds a new page to this layout.
	 */
	public void addPage(Page page) {
		page.setParentLayout(this);
		pages.add(page);
	}

	/**
	 * Transforms the given {@link LP} to a {@link FP} of the frame at this position.
	 * If there is no frame, null is returned.
	 */
	public FP getFP(LP lp) {
		if (lp == null)
			return null;
		Page page = pages.get(lp.pageIndex);
		return page.getFP(lp.position);
	}

	/**
	 * Call this method when the given score has been changed.
	 */
	public void scoreChanged(ScoreChangedEvent event) {
		updateScoreLayouts(event.getScore());
	}

	/**
	 * Computes the adjustment handle at the given position and returns it. If
	 * there is none, null is returned.
	 * @param layoutPosition the position where to look for a handle
	 * @param scaling the current scaling factor
	 */
	/* TODO
	public FrameHandle computeFrameHandleAt(LayoutPosition layoutPosition, float scaling)
	{
		if (layoutPosition == null)
			return null;
		// find handle on the given page
		FrameHandle handle = null;
		Page givenPage = pages.get(layoutPosition.getPageIndex());
		handle = givenPage.computeFrameHandleAt(layoutPosition.getPosition(), scaling);
		return handle;
	} */

	/**
	 * Gets the axis-aligned bounding rectangle of the given system, specified
	 * by its {@link ScoreLayout}, the index of the frame and the index of the
	 * system (relative to the frame) in page coordinates together with the
	 * index of the page. If not found, null is returned.
	 */
	// Experiment ---------------------------------------------------------------------
	public Tuple2<Integer, Rectangle2f> getSystemBoundingRect(ScoreLayout scoreLayout,
		int frameIndex, int systemIndex) {
		
		//find the frame
		//ScoreFrameChain chain = scoreLayouts.getKeyByValue(scoreLayout);
		ScoreFrameChain chain = getScoreFrameChain(scoreLayout.score);
		ScoreFrame scoreFrame = chain.getFrames().get(frameIndex);

		//get system boundaries in mm
		ScoreFrameLayout scoreFrameLayout = scoreLayout.frames.get(frameIndex);
		Rectangle2f rectMm = scoreFrameLayout.getSystemBoundaries(systemIndex);
		if (rectMm == null)
			return null;

		//compute corner points (because frame may be rotated) and transform them
		float x = rectMm.position.x - scoreFrame.getSize().width / 2;
		float y = rectMm.position.y - scoreFrame.getSize().height / 2;
		float w = rectMm.size.width;
		float h = rectMm.size.height;
		Point2f nw = scoreFrame.getPagePosition(new Point2f(x, y));
		Point2f ne = scoreFrame.getPagePosition(new Point2f(x + w, y));
		Point2f se = scoreFrame.getPagePosition(new Point2f(x + w, y + h));
		Point2f sw = scoreFrame.getPagePosition(new Point2f(x, y + h));

		// compute axis-aligned bounding box and return it
		Rectangle2f ret = new Rectangle2f(nw.x, nw.y, 0, 0);
		ret = ret.extend(ne);
		ret = ret.extend(se);
		ret = ret.extend(sw);

		int pageIndex = pages.indexOf(getPage(scoreFrame));
		return new Tuple2<Integer, Rectangle2f>(pageIndex, ret);
	} 

	/**
	 * Gets the parent {@link GroupFrame} of the given frame, or null, if there is none.
	 */
	public GroupFrame getParentGroupFrame(Frame frame)
	{
		if (parentFrames == null)
			updateCache();
		return parentFrames.get(frame);
	}
	
	
	/**
	 * Gets the parent {@link Page} of the given frame, or null, if it is not
	 * part of this layout.
	 */
	public Page getPage(Frame frame)
	{
		if (parentPages == null)
			updateCache();
		Page page = parentPages.get(frame);
		if (page != null)
			return page;
		GroupFrame parent = getParentGroupFrame(frame);
		if (parent != null)
			return getPage(parent);
		return null;
	}

	void updateCache()
	{
		//parent pages and group frames
		parentPages = new HashMap<Frame, Page>();
		parentFrames = new HashMap<Frame, GroupFrame>();
		for (Page page : pages)
		{
			for (Frame frame : page.getFrames())
			{
				parentPages.put(frame, page);
				if (frame instanceof GroupFrame)
				{
					addGroupFrameToCache((GroupFrame) frame);
				}
			}
		}
	}
	
	private void addGroupFrameToCache(GroupFrame groupFrame)
	{
		for (Frame frame : groupFrame.children)
		{
			parentFrames.put(frame, groupFrame);
			if (frame instanceof GroupFrame)
			{
				addGroupFrameToCache((GroupFrame) frame);
			}
		}
	}

	//---------------------------------------------------------------------------------------

	/**
	 * Gets the {@link ScoreFrame} which contains the given measure within
	 * the given score. If it can not be found, null is returned.
	 */
	@Untested public ScoreFrame getScoreFrame(Score score, int measure) {
		ScoreFrameChain chain = getScoreFrameChain(score);
		if (chain == null || chain.getScoreLayout() == null)
			return null;
		int frameIndex = chain.getScoreLayout().getFrameIndexOf(measure);
		if (frameIndex >= 0 && frameIndex < chain.getFrames().size())
			return chain.getFrames().get(frameIndex);
		return null;
	}

	/**
	 * Updates the {@link ScoreLayout}s belonging to the given {@link Score}.
	 */
	public void updateScoreLayouts(Score score) {
		ScoreFrameChain chain = getScoreFrameChain(score);
		if (chain == null)
			return;
		ScoreLayout oldScoreLayout = chain.getScoreLayout();

		//select symbol pool and layout settings
		SymbolPool symbolPool = oldScoreLayout != null ?
			oldScoreLayout.symbolPool : defaults.getSymbolPool();
		LayoutSettings layoutSettings = oldScoreLayout != null ?
			oldScoreLayout.layoutSettings : defaults.getLayoutSettings();

		CList<ScoreLayoutArea> areas = clist();
		for (ScoreFrame scoreFrame : chain.getFrames()) {
			areas.add(new ScoreLayoutArea(scoreFrame.getSize(), scoreFrame.getHFill(), scoreFrame.getVFill()));
		}
		areas.close();
		
		Context context = new Context(score, symbolPool, layoutSettings);
		Target target = new Target(areas, areas.get(areas.size() - 1), false);
		ScoreLayout scoreLayout = new ScoreLayouter(context, target).createScoreLayout();

		//set updated layout
		chain.setScoreLayout(scoreLayout);
	}

	/**
	 * Sets all pages to the given format.
	 * The defaults values are not changed.
	 */
	public void setLayoutFormat(LayoutFormat layoutFormat) {
		for (int i : range(pages)) {
			pages.get(i).setFormat(layoutFormat.getPageFormat(i));
		}
	}

	public void chainUpScoreFrame(ScoreFrame frameFrom, ScoreFrame frameTo) {
		if (frameFrom == frameTo)
			throw new IllegalArgumentException("Same frames");
		
		ScoreFrameChain fromChain = frameFrom.getScoreFrameChain();
		fromChain.add(frameFrom, frameTo);
	}

	/**
	 * Returns the {@link LP} of the given {@link MP} within the given {@link Score}
	 * at the given line position, or null if unknown.
	 */
	public LP computeLP(Score score, MP mp, float lp) {
		ScoreFrameChain chain = getScoreFrameChain(score);
		if (chain != null) {
			ScoreLayout sl = chain.getScoreLayout();
			ScoreLP slp = sl.getScoreLP(mp, lp);
			if (slp != null) {
				ScoreFrame frame = chain.getFrames().get(slp.frameIndex);
				Page page = frame.getParentPage();
				if (page != null) {
					Point2f frameP = slp.pMm.sub(frame.getSize().width / 2, frame.getSize().height / 2);
					int pageIndex = pages.indexOf(page);
					Point2f pMm = frame.getPagePosition(frameP);
					return lp(this, pageIndex, pMm);
				}
			}
		}
		return null;
	}

	/**
	 * Gets the {@link ScoreFrameChain} for the given {@link Score}, or null
	 * if it can not be found.
	 */
	public ScoreFrameChain getScoreFrameChain(Score score) {
		for (ScoreFrame frame : getScoreFrames()) {
			ScoreFrameChain chain = frame.getScoreFrameChain();
			if (chain != null && chain.getScore() == score)
				return chain;
		}
		return null;
	}

	/**
	 * Gets a list with all {@link ScoreFrame}s in this layout.
	 */
	public List<ScoreFrame> getScoreFrames() {
		List<ScoreFrame> ret = alist();
		for (Page page : pages) {
			for (Frame frame : page.getFrames()) {
				if (frame instanceof ScoreFrame)
					ret.add((ScoreFrame) frame);
				else if (frame instanceof GroupFrame)
					ret.addAll(((GroupFrame) frame).getScoreFrames());
			}
		}
		return ret;
	}

}
