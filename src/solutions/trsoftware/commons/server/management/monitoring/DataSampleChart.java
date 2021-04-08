/*
 * Copyright 2021 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package solutions.trsoftware.commons.server.management.monitoring;

import solutions.trsoftware.commons.server.servlet.UrlUtils;
import solutions.trsoftware.commons.shared.gchart.GoogleChart;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a Google Chart URL for reporting system load for a recent
 * window of time.
 *
 * @author Alex
 */
public class DataSampleChart {
  /**
   * According to http://code.google.com/apis/chart/image/faq.html#url_length -
   * 2048 is the hard limit for the URL length in GET requests
   * (NOTE: could have longer requests by using POST requests, see: http://code.google.com/apis/chart/docs/post_requests.html )
   * In actuality, though, 2048 is also too long sometimes (maybe for reasons like some kind of URL encoding that we're not seeing),
   * so we keep it at 1800 to be sure the chart will render properly.
   */
  private static final int URL_LENGTH_LIMIT = 1800;

  private StringBuilder dataPoints;
  private final StatType[] statTypes;
  private final long minTime;
  private final long maxTime;
  private final int width;
  private final int height;
  private long timeDelta;
  private double minY;
  private double maxY;
  private double yDelta;
  private String url;

  /**
   *
   * @param samples In non-decreasing order of time.
   * @param statTypes
   * @param minTime
   * @param maxTime
   * @param width
   * @param height
   */
  public <S extends StatType> DataSampleChart(List<? extends DataSample> samples, long minTime, long maxTime, int width, int height, S... statTypes) {
    this.statTypes = statTypes;
    this.minTime = minTime;
    this.maxTime = maxTime;
    this.width = width;
    this.height = height;
    // 1) find the min and max y-values, and count the number of points in the time range

    minY = Double.POSITIVE_INFINITY;
    maxY = Double.NEGATIVE_INFINITY;
    int pointsInTimeRange = 0;

    for (DataSample sample : samples) {
      long time = sample.getTime();
      if (time >= minTime && time <= maxTime) {
        pointsInTimeRange++;
        for (StatType type : statTypes) {
          double val = sample.getByStatType(type).doubleValue();
          if (val < minY)
            minY = val;
          if (val > maxY)
            maxY = val;
        }
      }
    }

    // 2) plot the x points as fraction of the last 24 hours and the y points as fraction of min-max delta

    // sample the samples such that we don't end up with more than maxDataPoints samples
    int maxDataPoints = (width - 30) / 2;
    double skipSize = Math.ceil((double)pointsInTimeRange / maxDataPoints);

    timeDelta = maxTime - minTime;
    yDelta = maxY - minY;

    // try increasing values of skipSize until we get a URL that fits under URL_LENGTH_LIMIT
    int maxSkipSize = samples.size() >> 3; // samples.size() / 8 is the limit, because no sense in having less than 8 points on the chart; skip 1.5 more points every time he URL is too long (similar to growing an array for an ArrayList)
    for (; url == null || skipSize < maxSkipSize; skipSize *= 1.5) {  // CAUTION: when multiplying by 1.5 skipSize must not be an int, otherwise the for loop might fail to make progress
      dataPoints = encodeDataPoints(samples, minTime, maxTime, (int)skipSize, statTypes);
      url = generateUrl();
      if (url.length() <= URL_LENGTH_LIMIT)
        break; // URL is good
    }
  }

  /**
   * Encodes the points from the given list of samples that fit within the given
   * time range, skipping every n-th point (n=skipSize)
   * @param skipSize-1 out of every skipSize data points will be skipped (so that the number of samples used will be samples.size()/skipSize)
   */
  private StringBuilder encodeDataPoints(List<? extends DataSample> samples, long minTime, long maxTime, int skipSize, StatType... statTypes) {
    StringBuilder xDataPoints = new StringBuilder(samples.size()*2);
    StringBuilder[] ySeries = new StringBuilder[statTypes.length];
    for (int i = 0; i < ySeries.length; i++) {
      ySeries[i] = new StringBuilder(samples.size()*2);
    }
    int i = 0;
    for (DataSample sample : samples) {
      long time = sample.getTime();
      if (time >= minTime && time <= maxTime) {
        if (skipSize < 2 || (i++ % skipSize == (skipSize - 1))) {
          xDataPoints.append(GoogleChart.extendedEncodePercentage((double)(sample.getTime() - minTime) / timeDelta));
          for (int j = 0; j < statTypes.length; j++) {
            ySeries[j].append(GoogleChart.extendedEncodePercentage((sample.getByStatType(statTypes[j]).doubleValue() - minY) / yDelta));
          }
        }
      }
    }

    StringBuilder points = new StringBuilder(xDataPoints.length()*4);
    for (StringBuilder yPoints : ySeries) {
      points.append(xDataPoints).append(",").append(yPoints).append(",");  // the xValues must be appended for each series
    }
    points.deleteCharAt(points.length()-1);  // remove the trailing comma
    return points;
  }

  private String generateUrl() {
    StringBuilder urlString = new StringBuilder(dataPoints.length()*4);
    long currentTime = System.currentTimeMillis();
    urlString.append(GoogleChart.BASE_URL).append("chxt=x,y&chd=e:")
        .append(dataPoints)
        .append("&chxp=0,97.6,73.2,48.8,24.5,0.1|1,20.0,40.0,60.0,80.0,100.0&chg=0,20.00,1,2&chm=R,7f7f7f,0,0.976,0.977|R,7f7f7f,0,0.732,0.733|R,7f7f7f,0,0.488,0.489|R,7f7f7f,0,0.245,0.246|R,7f7f7f,0,0.001,0.002|B,eaf0f4,0,0,0")
        .append("&chs=").append(width).append("x").append(height)
        .append("&cht=lxy&chxl=0:|")
        // generate the time labels, right-to-left
        .append("-").append(generateRelativeTimeLabel(currentTime - maxTime)).append('|')
        .append("-").append(generateRelativeTimeLabel(currentTime - (maxTime-(.25*timeDelta)))).append('|')
        .append("-").append(generateRelativeTimeLabel(currentTime - (maxTime-(.5*timeDelta)))).append('|')
        .append("-").append(generateRelativeTimeLabel(currentTime - (maxTime-(.75*timeDelta)))).append('|')
        .append("-").append(generateRelativeTimeLabel(currentTime - minTime)).append('|')
        .append("1:|")
        .append(statTypes[0].format(minY + .2*yDelta)).append('|')
        .append(statTypes[0].format(minY + .4*yDelta)).append('|')
        .append(statTypes[0].format(minY + .6*yDelta)).append('|')
        .append(statTypes[0].format(minY + .8*yDelta)).append('|')
        .append(statTypes[0].format(maxY))
        .append("&chls=2,0,0");
    if (statTypes.length > 1) {
      urlString.append("&chdl=");
      for (StatType statType : statTypes) {
        urlString.append(statType.getName()).append('|');
      }
      urlString.deleteCharAt(urlString.length()-1); // delete the last pipe
      urlString.append("&chco=0077cc,ff0000,00ff00,000000,aaaaaa");  // blue,red,green,black,gray; if more series than this, the colors will cycle
    }
    else {
      urlString.append("&chco=0077cc");
    }
    return urlString.toString();
  }

  private String generateRelativeTimeLabel(double millis) {
    if (millis < 1000)
      return "now";
    return UrlUtils.urlEncode(TimeUtils.timeIntervalToString(millis, "s", "s", "m", "m", "h", "h", "d", "d", "m", "m", "y", "y"));
  }

  public String getTitle() {
    return StringUtils.join(", ", statTypes) + " Chart";
  }

  public String getUrl() {
    return url;
  }

  // main method for manual testing (can copy & paste the URL into a browser)
  public static void main(String[] args) throws InterruptedException {
    ArrayList<SystemLoadSample> samples = new ArrayList<SystemLoadSample>();
    long t1 = System.currentTimeMillis();
    for (int i = 0; i < 2000; i++) {
      samples.add(new SystemLoadSample());
      Thread.sleep(1);
    }
    long t2 = System.currentTimeMillis();
    DataSampleChart chart = new DataSampleChart(samples, t1, t2, 750, 185, SystemLoadStatType.HEAP_USED, SystemLoadStatType.HEAP_COMMITTED, SystemLoadStatType.HEAP_TENURED_GEN);
    String url = chart.getUrl();
    System.out.println("url = " + url);
    System.out.println("url.length() = " + url.length());
  }
}
