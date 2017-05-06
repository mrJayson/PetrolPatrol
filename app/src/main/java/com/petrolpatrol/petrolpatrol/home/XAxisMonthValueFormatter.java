package com.petrolpatrol.petrolpatrol.home;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.petrolpatrol.petrolpatrol.util.TimeUtils;

import java.text.ParseException;
import java.util.List;

class XAxisMonthValueFormatter implements IAxisValueFormatter {

    private List<String> xLabels;

    XAxisMonthValueFormatter(List<String> xLabels) {
        this.xLabels = xLabels;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        try {
            String formattedValue = TimeUtils.dateToDayOfMonth(xLabels.get((int) value - 1));
            String appendix = "th";
            switch (formattedValue) {
                case "01":
                    appendix = "st";
                    break;
                case "02":
                    appendix = "nd";
                    break;
                case "03":
                    appendix = "rd";
                    break;
                case "21":
                    appendix = "st";
                    break;
                case "22":
                    appendix = "nd";
                    break;
                case "23":
                    appendix = "rd";
                    break;
                case "31":
                    appendix = "st";
                    break;
            }
            formattedValue = Integer.valueOf(formattedValue).toString();
            return formattedValue + appendix;
        } catch (ParseException e) {
            return "";
        }
    }
}
