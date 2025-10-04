package org.employee8.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Immutable Employee model (Java 8 style).
 * <p>Immutability improves reasoning and thread-safety.</p>
 */
public final class Employee {
    private final String id;
    private final String name;
    private final Department department;
    private final Double salary;
    private final LocalDate dateOfJoining;

    public Employee(String id, String name, Department department, Double salary, LocalDate dateOfJoining) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name , "name");
        this.department = Objects.requireNonNull(department, "department");
        if(salary <= 0) throw new IllegalArgumentException("salary must be positive");
        this.salary = salary;
        this.dateOfJoining = Objects.requireNonNull(dateOfJoining, "dateOfJoining");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Department getDepartment() {
        return department;
    }

    public Double getSalary() {
        return salary;
    }

    public LocalDate getDateOfJoining() {
        return dateOfJoining;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(id, employee.id) && Objects.equals(name, employee.name) && department == employee.department && Objects.equals(salary, employee.salary) && Objects.equals(dateOfJoining, employee.dateOfJoining);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, department, salary, dateOfJoining);
    }

    @Override public String toString() {
        return String.format("%s(%s) %s ₹%.2f DOJ:%s", name, id, department, salary, dateOfJoining);
    }
}
