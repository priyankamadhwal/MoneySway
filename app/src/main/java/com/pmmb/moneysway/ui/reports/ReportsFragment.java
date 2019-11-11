package com.pmmb.moneysway.ui.reports;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.pmmb.moneysway.R;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PieChartView;

public class ReportsFragment extends Fragment {

    private LineChartView lineChartView;
    private PieChartView income_pie_chart;
    private PieChartView expense_pie_chart;
    private ColumnChartView columnChart;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_reports, container, false);

        lineChartView = root.findViewById(R.id.income_expense_balance_chart);
        income_pie_chart = root.findViewById(R.id.income_pie_chart);
        expense_pie_chart = root.findViewById(R.id.expense_pie_chart);
        columnChart = root.findViewById(R.id.income_expense_category_wise_chart);

        showLineChart();
        showPieChart();
        showColumnChart();

        return root;
    }

    private void showLineChart() {
        int[] yAxisData = {50, 20, 15, 30, 20, 60, 15, 40, 45, 10, 90, 18};
        int[] yAxisData2 = {10, 23, 70, 99, 87, 55, 34, 10, 8, 90, 100, 6};
        int[] yAxisData3 = {40, -3, 89, 10, 10, 20, 15, 70, 0, 50, 30, 90};

        String[] axisData = {"2/11", "4/11", "6/11", "10/11", "12/11", "14/11", "16/11", "18/11", "20/11",
                "26/11", "28/11", "30/11"};
        List axisValues = new ArrayList();
        for(int i = 0; i < axisData.length; i++){
            axisValues.add(i, new AxisValue(i).setLabel(axisData[i]));
        }
        List lines = new ArrayList();
        lines.add(createLine(yAxisData, "#03A9F4"));
        lines.add(createLine(yAxisData2, "#bf0600"));
        lines.add(createLine(yAxisData3, "#12a602"));
        LineChartData data = new LineChartData();
        data.setLines(lines);
        Axis axis = new Axis();
        axis.setValues(axisValues);
        data.setAxisXBottom(axis);
        Axis yAxis = new Axis();
        data.setAxisYLeft(yAxis);
        axis.setTextSize(12);
        yAxis.setTextSize(12);
        yAxis.setName("Money flows");
        axis.setHasLines(true);
        yAxis.setHasLines(true);
        lineChartView.setLineChartData(data);
        Viewport viewport = new Viewport(lineChartView.getMaximumViewport());
        viewport.top =110;
        lineChartView.setMaximumViewport(viewport);
        lineChartView.setCurrentViewport(viewport);
    }

    private Line createLine(int[] yAxisData, String color) {
        List yAxisValues = new ArrayList();
        for (int i = 0; i < yAxisData.length; i++){
            yAxisValues.add(new PointValue(i, yAxisData[i]));
        }
        Line line = new Line(yAxisValues).setColor(Color.parseColor(color)).setStrokeWidth(1).setPointRadius(2).setCubic(true);
        return line;
    }

    private int getSign() {
        int[] sign = new int[]{-1, 1};
        return sign[Math.round((float) Math.random())];
    }

    private void showPieChart() {

        PieChartData income_data= getPieChartData(5,"Incomes");
        income_pie_chart.setCircleFillRatio(0.7f);
        income_pie_chart.setPieChartData(income_data);

        PieChartData expense_data= getPieChartData(6,"Expenses");
        expense_pie_chart.setCircleFillRatio(0.7f);
        expense_pie_chart.setPieChartData(expense_data);

    }

    private PieChartData getPieChartData(int numSlices, String centerText) {
        List<SliceValue> values = new ArrayList<SliceValue>();
        for (int i = 0; i < numSlices; ++i) {
            SliceValue sliceValue = new SliceValue((float) Math.random() * 30 + 15, ChartUtils.pickColor());
            values.add(sliceValue);
        }
        PieChartData data = new PieChartData(values);
        data.setHasCenterCircle(true);
        data.setHasLabels(true);
        data.setHasLabelsOutside(true);
        data.setCenterText1(centerText);
        data.setCenterText1FontSize(16);
        return data;
    }

    private void showColumnChart() {
        int numSubcolumns = 6;
        int numColumns = 30;
        // Column can have many stacked subcolumns, here I use 4 stacke subcolumn in each of 4 columns.
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        for (int i = 0; i < numColumns; ++i) {
            values = new ArrayList<SubcolumnValue>();
            for (int j = 0; j < numSubcolumns; ++j) {
                int sign = getSign();
                values.add(new SubcolumnValue((float) Math.random() * 20f * sign + 5 * sign, ChartUtils.pickColor()));
            }
            Column column = new Column(values);
            column.setHasLabelsOnlyForSelected(true);
            columns.add(column);
        }

        ColumnChartData columnChartData = new ColumnChartData(columns);
        columnChartData.setStacked(true);
        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);
        axisX.setName("Date");
        axisY.setName("Category-wise income/expenses");
        columnChartData.setAxisXBottom(axisX);
        columnChartData.setAxisYLeft(axisY);
        columnChart.setColumnChartData(columnChartData);
    }
}