package com.example.nativelogreader;

import java.util.List;

public class ErrorPattern {
    public String pattern;
    public String errorName;
    public String meaning;
    public List<String> causes;
    public List<String> solutions;

    public ErrorPattern(String pattern, String errorName, String meaning, List<String> causes, List<String> solutions) {
        this.pattern = pattern;
        this.errorName = errorName;
        this.meaning = meaning;
        this.causes = causes;
        this.solutions = solutions;
    }
}
