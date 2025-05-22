package com.mocktutorial.core.v3;
import org.springframework.beans.factory.annotation.Autowired;
public class SpringBean {
    @Autowired(required = false)
    @MockField
    public SampleService sampleService;
    public SpringBean() {
        System.err.println("[SpringBean] Constructor called");
    }
    public String call(String name) {
        System.err.println("[SpringBean] call() sampleService: " + sampleService);
        return sampleService.hello(name);
    }
} 