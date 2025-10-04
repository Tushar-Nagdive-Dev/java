package org.employee8.service;

import org.employee8.domain.Department;
import org.employee8.domain.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class EmployeeAnalyticsTest {

    private List<Employee> data;
    private EmployeeAnalytics analytics;

    @BeforeEach
    void setUp() {
        analytics = new EmployeeAnalytics();
        data = Arrays.asList(
                new Employee("E01", "Tushar", Department.IT, 140_000.0, LocalDate.of(2022, 6, 10)),
                new Employee("E02", "Maya",   Department.FINANCE, 120_000.0, LocalDate.of(2021,11, 5)),
                new Employee("E03", "Alex",   Department.IT, 180_000.0, LocalDate.of(2023, 2,18)),
                new Employee("E04", "Riya",   Department.HR, 95_000.0, LocalDate.of(2020, 4,30)),
                new Employee("E05", "Dev",    Department.SALES, 110_000.0, LocalDate.of(2024, 1,12)),
                new Employee("E06", "Sam",    Department.OPS, 105_000.0, LocalDate.of(2022, 8,25)),
                new Employee("E07", "Neha",   Department.IT, 125_000.0, LocalDate.of(2024, 9, 1))
        );
    }

    @Test
    void byDepartment_filtersCorrectly() {
        List<Employee> it = analytics.byDepartment(data, Department.IT);
        assertThat(it.stream().map(Employee::getName).toList())
                .containsExactlyInAnyOrder("Tushar", "Alex", "Neha");
    }

    @Test
    void topEarners_returnsSortedTopN() {
        List<Employee> top2 = analytics.topEarners(data, 2);
        assertThat(top2).extracting(Employee::getId).containsExactly("E03", "E01"); // 180k, 140k
    }

    @Test
    void totalSalary_sumsAll() {
        double total = analytics.totalSalary(data);
        assertThat(total).isEqualTo(140_000 + 120_000 + 180_000 + 95_000 + 110_000 + 105_000 + 125_000);
    }

    @Test
    void highestPaid_returnsOptional() {
        assertThat(analytics.highestPaid(data))
                .isPresent()
                .get()
                .extracting(Employee::getId)
                .isEqualTo("E03");
    }

    @Test
    void namesJoinedAfter_filtersAndMaps() {
        List<String> names = analytics.namesJoinedAfter(data, LocalDate.of(2023,1,1));
        assertThat(names).containsExactlyInAnyOrder("Alex", "Dev", "Neha");
    }

    @Test
    void partitionBySalary_partitionsOnThreshold() {
        Map<Boolean, List<Employee>> parts = analytics.partitionBySalary(data, 120_000);
        assertThat(parts.get(true)).extracting(Employee::getId).contains("E01","E02","E03","E07");
        assertThat(parts.get(false)).extracting(Employee::getId).contains("E04","E05","E06");
    }

    @Test
    void distinctDepartmentsCsv_isStableAndSorted() {
        String csv = analytics.distinctDepartmentsCsv(data);
        assertThat(csv).isEqualTo("FINANCE,HR,IT,OPS,SALES");
    }

    @Test
    void findById_worksWithOptional() {
        assertThat(analytics.findById(data, "E05")).isPresent();
        assertThat(analytics.findById(data, "NOPE")).isNotPresent();
    }

}
