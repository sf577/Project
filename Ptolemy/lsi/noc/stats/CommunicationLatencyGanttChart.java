package lsi.noc.stats;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;

public class CommunicationLatencyGanttChart extends
		BasicCommunicationLatencyAnalysis {

	public CommunicationLatencyGanttChart() {

		super();

		JFreeChart chart = ChartFactory.createGanttChart("Gantt Chart Demo", // chart
																				// title
				"Task", // domain axis label
				"Date", // range axis label
				createDataset(), // data
				true, // include legend
				true, // tooltips
				false // urls
				);

		tabbedPane.addTab("Gantt Chart", new ChartPanel(chart));

	}

	public IntervalCategoryDataset createDataset() {

		final TaskSeries s1 = new TaskSeries("00-01");
		s1.add(new Task("T1", new SimpleTimePeriod(0, 200)));
		s1.add(new Task("T2", new SimpleTimePeriod(10, 200)));
		s1.add(new Task("T1", new SimpleTimePeriod(210, 400)));

		TaskSeriesCollection collection = new TaskSeriesCollection();
		collection.add(s1);

		return collection;
	}

}
