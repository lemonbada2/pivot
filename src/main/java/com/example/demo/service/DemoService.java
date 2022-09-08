package com.example.demo.service;

import com.example.demo.source.DatabaseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DemoService {

    @Autowired
    private DatabaseMapper databaseMapper;


    public List<String> extractColumns(String current, List<List<String>> list) {
        List<String> lst = new ArrayList<>();
        if (list.size() == 0) {
            return Collections.singletonList(current);
        } else {
            for (String str : list.get(0)) {
                lst.addAll(extractColumns((current + "^" + str), list.stream().skip(1).collect(Collectors.toList())));
            }
        }
        return lst;
    }

    public List<List<String>> extractColumns(List<List<String>> list) {
        List<List<String>> lst = new ArrayList<>();
        for (String str : list.get(0)) {
            lst.add(extractColumns(str, list.stream().skip(1).collect(Collectors.toList())));
        }
        return lst;
    }

    public void pivot() {

        var baseQuery = "" +
                "with data as (" +
                "   select * from a_pivot" +
                ")" +
                "select * from data";

        var info = PivotInfo.builder()
                .axis(Arrays.asList("id"))
                .legend(Arrays.asList("dt", "exam"))
                .value(Arrays.asList(
                        PivotInfo.PivotValue.builder()
                                .aggregation("sum")
                                .value("qty")
                                .build(),
                        PivotInfo.PivotValue.builder()
                                .aggregation("max")
                                .value("exam")
                                .build()))
                .build();

        // 데이터
        var data = databaseMapper.execute(baseQuery);

        // 데이터 (행 -> 열)
        List<List<String>> columnData = new ArrayList<>();
        for (String fieldToColumn : info.getLegend()) {

            List<String> columns = data.stream()
                    .map(d -> d.get(fieldToColumn).toString())
                    .distinct()
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList());

            columnData.add(columns);
        }

        var extractedColumns = extractColumns(columnData);

        String fieldToColumn = "";

        List<String> cases = new ArrayList<>();

        String whenClause = "";
        String column = "";

        for (List<String> cols : extractedColumns) {


            for (String tokenizer : cols) {
                List<String> cc = new ArrayList<>();
                var tmp = Arrays.asList(tokenizer.split("\\^"));
                for (int i = 0; i < info.getLegend().size(); i++) {
                    cc.add(MessageFormat.format("{0} = ''{1}''", info.getLegend().get(i), tmp.get(i)));
                }

                for(PivotInfo.PivotValue pv : info.getValue()) {
                    cases.add(MessageFormat.format("{3}(case when {0} then {1} else null end) ''{2}''",
                            String.join(" and ", cc),
                            pv.getValue(),
                            tokenizer + "_" + pv.getValue(),
                            pv.getAggregation()));
                }

            }
        }


        var query = "select ";

        if (info.getAxis().size() == 0)
            info.setAxis(Arrays.asList("id"));

        query += MessageFormat.format(" {0} ,", String.join(",", info.getAxis()));

        query += cases.stream().collect(Collectors.joining("," + System.lineSeparator()));
        query += MessageFormat.format("from ({0}) d", baseQuery);

        query += MessageFormat.format(" group by {0}", String.join(",", info.getAxis()));

        var rows = databaseMapper.execute(query);
        rows.forEach(row -> {
            System.out.println(row);
        });


    }
}
