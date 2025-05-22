package com.mocktutorial.core.v3;

import java.lang.instrument.Instrumentation;

/**
 * V3 MockAgent: Java Agent入口，负责Instrumentation注入与管理。
 * 支持-premain和agentmain两种模式。
 */
public class MockAgent {
    private static volatile Instrumentation instrumentation;

    public static void premain(String agentArgs, Instrumentation inst) {
        instrumentation = inst;
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        instrumentation = inst;
    }

    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public static boolean isAvailable() {
        return instrumentation != null;
    }
} 