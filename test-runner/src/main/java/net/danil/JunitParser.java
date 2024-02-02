package net.danil;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import net.danil.generated.junit.Testsuite;
import net.danil.generated.junit.Testsuites;

import java.util.Objects;

public class JunitParser {
    public static void main(String... args) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance("net.danil.generated.junit");
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
//        final var xmlReport = JunitParser.class.getClassLoader().getResourceAsStream("cpp-google-test.xml");
//        final var xmlReport = JunitParser.class.getClassLoader().getResourceAsStream("java-junit-test.xml");
        final var xmlReport = JunitParser.class.getClassLoader().getResourceAsStream("js-jest-test.xml");

//        var testsuite = (Testsuite) unmarshaller.unmarshal(xmlReport);
//        final var good = testsuite.getTestcase().stream().allMatch(testcase ->
//                testcase.getSkipped() == null &&
//                        (testcase.getError() == null || testcase.getError().isEmpty()) &&
//                        (testcase.getFailure() == null || testcase.getFailure().isEmpty())
//        );
//        System.out.println(testsuite.getTestcase());

        var testsuites = (Testsuites) unmarshaller.unmarshal(xmlReport);
        final var good = testsuites.getTestsuite().stream().allMatch(testsuite -> {
            return testsuite.getTestcase().stream().allMatch(testcase ->
                    testcase.getSkipped() == null &&
                            (testcase.getError() == null || testcase.getError().isEmpty()) &&
                            (testcase.getFailure() == null || testcase.getFailure().isEmpty())
            );
        });
        System.out.println(testsuites);

        System.out.println("it's " + (good ? "good" : "bad"));
    }
}
