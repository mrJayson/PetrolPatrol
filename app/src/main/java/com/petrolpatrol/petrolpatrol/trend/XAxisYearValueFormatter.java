package com.petrolpatrol.petrolpatrol.trend;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.petrolpatrol.petrolpatrol.util.TimeUtils;

import java.text.ParseException;
import java.util.List;

/**
 * Created by jason on 4/03/17.
 */
public class XAxisYearValueFormatter implements IAxisValueFormatter {

    private List<String> xLabels;

    public XAxisYearValueFormatter(List<String> xLabels) {
        this.xLabels = xLabels;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        try {
            return TimeUtils.dateToMonthOfYear(xLabels.get((int) value - 1));
        } catch (ParseException e) {
            return "";
        }
    }
}
