package net.danil.web.service;

import net.danil.web.dto.TestResult;
import org.springframework.stereotype.Service;

@Service
public class TestResultAnalyzerService {
    public record Verdict(boolean solved) {
    }
    public Verdict judgeResults(TestResult testResult) {
        if(testResult.statusCode() != 0)
            return new Verdict(false);
        if(testResult.failures() != 0 || testResult.errors() != 0)
            return new Verdict(false);
        if(testResult.tests() == 0)
            return new Verdict(false);

        return new Verdict(true);
    }
}
