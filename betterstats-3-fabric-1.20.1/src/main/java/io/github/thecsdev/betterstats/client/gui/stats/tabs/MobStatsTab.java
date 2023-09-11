package io.github.thecsdev.betterstats.client.gui.stats.tabs;

import static io.github.thecsdev.betterstats.api.client.gui.stats.widget.MobStatWidget.SIZE;
import static io.github.thecsdev.betterstats.api.util.stats.SUMobStat.getMobStatsByModGroups;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.Collection;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Internal;

import io.github.thecsdev.betterstats.api.client.gui.stats.widget.MobStatWidget;
import io.github.thecsdev.betterstats.api.util.stats.SUMobStat;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.util.TUtils;
import io.github.thecsdev.tcdcommons.api.util.annotations.Virtual;
import net.minecraft.text.Text;

@Internal @Virtual class MobStatsTab extends BSStatsTabs<SUMobStat>
{
	// ==================================================
	public @Virtual @Override Text getName() { return translatable("stat.mobsButton"); }
	// --------------------------------------------------
	public @Virtual @Override void initStats(StatsInitContext initContext)
	{
		final var panel = initContext.getStatsPanel();
		final var stats = initContext.getStatsProvider();
		
		for(final var statGroup : getMobStatsByModGroups(stats, getPredicate(initContext.getFilterSettings())).entrySet())
		{
			BSStatsTabs.init_groupLabel(panel, literal(TUtils.getModName(statGroup.getKey())));
			init_stats(panel, statGroup.getValue(), null);
		}
	}
	// --------------------------------------------------
	/**
	 * Initializes the GUI for a {@link Collection} of {@link SUMobStat}s.
	 */
	protected static void init_stats
	(TPanelElement panel, Collection<SUMobStat> stats, Consumer<MobStatWidget> processWidget)
	{
		final int wmp = panel.getWidth() - (panel.getScrollPadding() * 2); //width minus padding
		int nextX = panel.getScrollPadding();
		int nextY = BSStatsTabs.nextBottomY(panel) - panel.getY();
		
		for(final SUMobStat stat : stats)
		{
			final var statElement = new MobStatWidget(nextX, nextY, stat);
			panel.addChild(statElement, true);
			if(processWidget != null)
				processWidget.accept(statElement);
			
			nextX += SIZE + GAP;
			if(nextX + SIZE >= wmp)
			{
				nextX = panel.getScrollPadding();
				nextY = (BSStatsTabs.nextBottomY(panel) - panel.getY()) + GAP;
			}
		}
	}
	// ==================================================
}