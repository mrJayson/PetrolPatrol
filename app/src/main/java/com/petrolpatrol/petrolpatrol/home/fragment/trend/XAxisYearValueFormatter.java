package com.petrolpatrol.petrolpatrol.home.fragment.trend;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.petrolpatrol.petrolpatrol.util.TimeUtils;

import java.text.ParseException;
import java.util.List;

class XAxisYearValueFormatter implements IAxisValueFormatter {

    private List<String> xLabels;

    XAxisYearValueFormatter(List<String> xLabels) {
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
