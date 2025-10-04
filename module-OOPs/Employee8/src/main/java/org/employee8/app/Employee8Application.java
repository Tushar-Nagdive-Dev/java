package org.employee8.app;

import org.employee8.domain.Department;
import org.employee8.domain.Employee;
import org.employee8.service.EmployeeAnalytics;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class Employee8Application {
    public static void main(String[] args) {
        List<Employee> employees = sampleData();
        EmployeeAnalytics analytics = new EmployeeAnalytics();

        System.out.println("— IT Employees —");
        analytics.byDepartment(employees, Department.IT).forEach(System.out::println);

        System.out.println("\n— Top 3 Earners —");
        analytics.topEarners(employees, 3).forEach(System.out::println);

        System.out.println("\nTotal salary: ₹" + analytics.totalSalary(employees));

        System.out.println("\n— Avg Salary by Dept —");
        analytics.avgSalaryByDept(employees).forEach((d, avg) -> System.out.println(d + ": " + String.format("%.2f", avg)));

        System.out.println("\nHighest Paid: " + analytics.highestPaid(employees).map(Employee::toString).orElse("none"));

        System.out.println("\nJoined after 2023-01-01: " + analytics.namesJoinedAfter(employees, LocalDate.of(2023, 1, 1)));

        System.out.println("\nPartitions by ₹100k: " + analytics.partitionBySalary(employees, 100_000));

        System.out.println("\nDept CSV: " + analytics.distinctDepartmentsCsv(employees));

        System.out.println("\n— Total Salary by Dept (parallel) —");

        System.out.println("\n Total Salary: "+ analytics.totalSalaryByDeptParallel(employees));
    }

    /** Sample data generator — pure Java 8 (no loops, uses Stream API). */
    private static List<Employee> sampleData() {
        return Arrays.asList(
                new Employee("E01", "Tushar", Department.IT, 140_000.0, LocalDate.of(2022,  6, 10)),
                new Employee("E02", "Maya",   Department.FINANCE, 120_000.0, LocalDate.of(2021, 11,  5)),
                new Employee("E03", "Alex",   Department.IT, 180_000.0, LocalDate.of(2023,  2, 18)),
                new Employee("E04", "Riya",   Department.HR, 95_000.0, LocalDate.of(2020,  4, 30)),
                new Employee("E05", "Dev",    Department.SALES, 110_000.0, LocalDate.of(2024,  1, 12)),
                new Employee("E06", "Sam",    Department.OPS, 105_000.0, LocalDate.of(2022,  8, 25)),
                new Employee("E07", "Neha",   Department.IT, 125_000.0, LocalDate.of(2024,  9,  1))
        );
    }
}
