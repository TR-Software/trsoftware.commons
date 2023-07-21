package solutions.trsoftware.commons.client.debug.profiler;

import com.google.common.collect.ImmutableMap;
import com.google.gwt.user.cellview.client.TextColumn;
import solutions.trsoftware.commons.client.cellview.NumberColumn;
import solutions.trsoftware.commons.client.cellview.SortableCellTable;
import solutions.trsoftware.commons.shared.util.stats.ImmutableStats;
import solutions.trsoftware.commons.shared.util.stats.SampleStatistics;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Alex
 * @since 5/8/2023
 */
class ProfilerStatsTable extends SortableCellTable<ProfilerStatsSample> {

  public ProfilerStatsTable() {
    super(ProfilerStatsSample::getName);
    addSortableColumn("Name", new TextColumn<ProfilerStatsSample>() {
      @Override
      public String getValue(ProfilerStatsSample sample) {
        return sample.getName();
      }
    });

    addStatColumn("Calls", SampleStatistics::size);
    addStatColumn("Mean", SampleStatistics::mean);
    addStatColumn("Min", SampleStatistics::min);
    addStatColumn("Max", SampleStatistics::max);

  }

  private void addStatColumn(String name, Function<SampleStatistics<? extends Number>, ? extends Number> valueExtractor) {
    addStatColumn(name, new NumberColumn<ProfilerStatsSample>(1) {
      @Override
      public Number getValue(ProfilerStatsSample sample) {
        return valueExtractor.apply(sample.getStats());
      }
    });
  }

  private void addStatColumn(String name, NumberColumn<ProfilerStatsSample> numberColumn) {
    addSortableColumn(name, numberColumn, Comparator.comparing(Number::doubleValue))
        .setDefaultSortAscending(false);  // descending order by default
  }

  public void refresh() {
    setData(fetchData());
  }


  protected List<ProfilerStatsSample> fetchData() {
    ImmutableMap<String, ImmutableStats<Double>> statsByName = Profiler.getInstance().getStats();
    return statsByName.entrySet().stream()
        .map(ProfilerStatsSample::new)
        .collect(Collectors.toList());
  }
}
