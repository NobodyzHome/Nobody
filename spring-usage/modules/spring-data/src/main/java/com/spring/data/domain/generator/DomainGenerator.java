package com.spring.data.domain.generator;

import com.spring.data.domain.BaseDept;
import com.spring.data.domain.BaseLine;
import com.spring.data.domain.BaseTerminal;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DomainGenerator {

    public static BaseDept generateDept(Integer deptNo) {
        BaseDept dept = new BaseDept();
        dept.setDeptNo(deptNo);
        dept.setDeptCode("No." + dept.getDeptNo());
        dept.setDeptName(dept.getDeptNo() + "公司");
        dept.setDeptEnName("This is the " + dept.getDeptCode() + " dept!");
        dept.setLevel(RandomUtils.nextInt(1, 20));
        dept.setRemark(RandomStringUtils.random(30, true, true));
        dept.setCreateTime(DateUtils.addMinutes(new Date(), RandomUtils.nextInt(10, 200)));

        return dept;
    }

    public static List<BaseDept> generateDepts(Integer... deptNoArray) {
        List<BaseDept> baseDepts = Arrays.stream(deptNoArray).map(DomainGenerator::generateDept).collect(Collectors.toList());

        return baseDepts;
    }

    public static List<BaseDept> generateDepts(Integer count) {
        Integer[] deptNoArray = new Integer[count];
        for (int i = 0; i < count; i++) {
            deptNoArray[i] = RandomUtils.nextInt(1000, 2000);
        }
        List<BaseDept> baseDepts = generateDepts(deptNoArray);
        return baseDepts;
    }

    public static BaseLine generateLine(Integer lineNo, boolean saveId) {
        BaseLine line = new BaseLine();
        line.setLineNo(lineNo);
        line.setLineCode("No is " + line.getLineNo() + "!");
        line.setLineName("第" + line.getLineNo() + "路");
        line.setLineEnName("There are many good goods!They are going away!" + line.getLineCode());
        line.setCreateTime(new Date());
        line.setUpdateTime(DateUtils.addHours(line.getCreateTime(), RandomUtils.nextInt(1, 10)));
        line.setMemo("There are many good goods!天气可以不错!");
        if (saveId) {
            line.setId(line.getLineNo().toString());
        }

        return line;
    }

    public static List<BaseLine> generateLines(boolean saveId, Integer... lineNoArray) {
        if (ArrayUtils.isNotEmpty(lineNoArray)) {
            List<BaseLine> baseLineList = Arrays.stream(lineNoArray).map(lineNo -> generateLine(lineNo, saveId)).collect(Collectors.toList());
            return baseLineList;
        }
        return null;
    }

    public static List<BaseLine> generateLines(boolean saveId, Integer lineCount) {
        Integer[] lineNoArray = new Integer[lineCount];
        for (int index = 0; index < lineNoArray.length; index++) {
            lineNoArray[index] = RandomUtils.nextInt(1000, 5000);
        }

        return generateLines(saveId, lineNoArray);
    }

    public static BaseTerminal generateTerminal(Integer terminalNo) {
        BaseTerminal terminal = new BaseTerminal();
        terminal.setTerminalNo(String.valueOf(terminalNo));
        terminal.setTerminalCode("The NUMBER of this TERMINAL is " + terminalNo + "!");
        terminal.setPlateCode("京A" + RandomUtils.nextInt(100000, 999999));
        terminal.setCreateTime(new Date());
        terminal.setUpdateTime(DateUtils.addDays(terminal.getCreateTime(), RandomUtils.nextInt(10, 300) * (terminalNo < 1000 ? 1 : -1)));
        terminal.setMemo("今天天气VERY nice！");
        terminal.setLineNo(RandomUtils.nextInt(100, 1000));
        terminal.setRemark("there aRE many PEOPLES on the ground!");
        terminal.setTerminalName(String.format("第%s路的第%s车", terminal.getLineNo(), terminal.getTerminalNo()));

        return terminal;
    }

    public static List<BaseTerminal> generateTerminals(Integer... terminalNoArray) {
        if (ArrayUtils.isNotEmpty(terminalNoArray)) {
            List<BaseTerminal> baseTerminalList = Arrays.stream(terminalNoArray).map(terminalNo -> generateTerminal(terminalNo)).collect(Collectors.toList());
            return baseTerminalList;
        }
        return null;
    }

    public static List<BaseTerminal> generateTerminals(Integer terminalCount) {
        Integer[] terminalNoArray = new Integer[terminalCount];
        for (int index = 0; index < terminalNoArray.length; index++) {
            terminalNoArray[index] = RandomUtils.nextInt(500, 1500);
        }

        return generateTerminals(terminalNoArray);
    }
}
