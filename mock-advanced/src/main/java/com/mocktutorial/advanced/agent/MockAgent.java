package com.mocktutorial.advanced.agent;

import com.mocktutorial.advanced.internal.StaticBytecodeEnhancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;

/**
 * Java agent for the enhanced mock framework.
 * This agent provides instrumentation capabilities required for
 * advanced features like static method mocking.
 */
public class MockAgent {
    private static final Logger logger = LoggerFactory.getLogger(MockAgent.class);
    
    /**
     * Premain method called when the JVM starts with this agent.
     * 
     * @param agentArgs agent arguments
     * @param inst instrumentation instance
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        logger.info("Enhanced Mock Framework Agent initialized");
        StaticBytecodeEnhancer.setInstrumentation(inst);
    }
    
    /**
     * Agentmain method called when the agent is attached to a running JVM.
     * 
     * @param agentArgs agent arguments
     * @param inst instrumentation instance
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        logger.info("Enhanced Mock Framework Agent attached to running JVM");
        StaticBytecodeEnhancer.setInstrumentation(inst);
    }
}