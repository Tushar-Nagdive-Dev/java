package org.employee8.service;

import org.employee8.domain.Department;
import org.employee8.domain.Employee;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * Pure Java 8 analytics over Employees.
 * <p>Only uses Streams, Collectors, Optionals, Method References.</p>
 */
public final class EmployeeAnalytics {

    /** Filter employees by department (Predicate + Stream). */
    public List<Employee> byDepartment(List<Employee> all,Department department) {
        return all.stream().filter(e -> e.getDepartment() == department).collect(toList());
    }

    /** Top N by salary (sorted + limit). */
    public List<Employee> topEarners(List<Employee> all, Integer n) {
        return all.stream().sorted(Comparator.comparingDouble(Employee::getSalary).reversed()).limit(n).collect(toList());
    }

    /** Total payroll using reduce (map + reduce). */
    public double totalSalary(List<Employee> all) {
        //return all.stream().mapToDouble(Employee::getSalary).sum();
        return all.stream().map(Employee::getSalary).reduce(0.0, Double::sum);
    }

    /** Average salary per department (groupingBy + averagingDouble). */
    public Map<Department, Double> avgSalaryByDept(List<Employee> all) {
        return all.stream().collect(Collectors.groupingBy(Employee::getDepartment, averagingDouble(Employee::getSalary)));
    }

    /** Group employees by department (groupingBy). */
    public Map<Department, List<Employee>> groupByDept(List<Employee> all) {
        return all.stream().collect(Collectors.groupingBy(Employee::getDepartment));
    }

    /** Highest paid employee (max + Optional). */
    public Optional<Employee> highestPaid(List<Employee> all) {
        return all.stream().max(Comparator.comparingDouble(Employee::getSalary));
    }

    /** Names of people who joined after a given date (filter + map + toList). */
    public List<String> namesJoinedAfter(List<Employee> all, LocalDate cutOffDate) {
        return all.stream().filter(e -> e.getDateOfJoining().isAfter(cutOffDate)).map(Employee::getName).collect(toList());
    }

    /** Partition employees into high vs low earners using a threshold (partitioningBy). */
    public Map<Boolean, List<Employee>> partitionBySalary(List<Employee> all, double threshold) {
        return all.stream().collect(Collectors.partitioningBy(e -> e.getSalary() >= threshold));
    }

    /** Distinct department names sorted (map + distinct + sorted + joining). */
    public String distinctDepartmentsCsv(List<Employee> all) {
        return all.stream().map(e -> e.getDepartment().name()).distinct().sorted().collect(joining(","));
    }

    /** Safe find by id (filter + findFirst + Optional). */
    public Optional<Employee> findById(List<Employee> all, String id) {
        return all.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    /** Parallel example: total salary per dept using parallel stream (demo only). */
    public Map<Department, Double> totalSalaryByDeptParallel(List<Employee> all) {
        return all.parallelStream() // use for CPU-bound & large data; avoid for I/O
                .collect(groupingByConcurrent(Employee::getDepartment, summingDouble(Employee::getSalary)));
    }
}
