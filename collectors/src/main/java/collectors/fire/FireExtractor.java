package collectors.fire;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 화재 1단계: 서울 열린데이터 통계표(장소별 화재발생(동별), SpreadsheetML) → data/fire.json
 * 입력: data/201_DT_201004_O160028_2020_20260720162947.xls (EUC-KR XML, 세로형)
 * - 장소별 == '합계' 행만 사용 (동별 연도별 총 발생 건수)
 * - 값 '' : 해당 연도에 동이 존재하지 않음(분동 전/후) → 레코드 생략
 * - 값 '-' : 0건
 * - 강일동처럼 같은 동이 구계열/신계열 두 행으로 나뉜 경우 연도 구간이 겹치지 않으므로 병합
 */
public class FireExtractor {

    private static final Set<String> GU = Set.of(
            "종로구", "중구", "용산구", "성동구", "광진구", "동대문구", "중랑구", "성북구",
            "강북구", "도봉구", "노원구", "은평구", "서대문구", "마포구", "양천구", "강서구",
            "구로구", "금천구", "영등포구", "동작구", "관악구", "서초구", "강남구", "송파구", "강동구");

    public static void main(String[] args) throws Exception {
        Path input = Path.of("data", "201_DT_201004_O160028_2020_20260720162947.xls");
        String xml = new String(Files.readAllBytes(input), Charset.forName("MS949")).stripLeading();

        // 두 번째 워크시트(메타정보)에 이스케이프되지 않은 <, & 가 있어 첫 워크시트까지만 파싱
        int end = xml.indexOf("</Worksheet>");
        xml = xml.substring(0, end + "</Worksheet>".length()) + "</Workbook>";

        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));

        Element sheet = (Element) doc.getElementsByTagName("Worksheet").item(0);
        NodeList rows = sheet.getElementsByTagName("Row");

        List<String> years = null;
        String currentGu = null;
        // key = "구|동|연도", 강일동 등 분동 전/후 두 행을 하나로 병합하기 위해 맵 사용
        Map<String, Integer> merged = new LinkedHashMap<>();

        for (int i = 0; i < rows.getLength(); i++) {
            List<String> cells = readCells((Element) rows.item(i));
            if (cells.size() < 2) continue;

            String region = cells.get(0);
            if (years == null) {
                if ("동별".equals(region)) {
                    years = new ArrayList<>();
                    for (String c : cells) {
                        if (c != null && c.matches("\\d{4}.*")) years.add(c.substring(0, 4));
                    }
                }
                continue;
            }

            if (GU.contains(region)) currentGu = region;
            if ("합계".equals(region) || GU.contains(region)) continue;
            if (!"합계".equals(cells.get(1))) continue;
            if (currentGu == null) throw new IllegalStateException("구 정보 없이 동 행 등장: " + region);

            for (int y = 0; y < years.size(); y++) {
                int col = 4 + y;
                String v = col < cells.size() ? cells.get(col) : null;
                if (v == null || v.isBlank()) continue;          // 해당 연도에 미존재
                int cnt = "-".equals(v) ? 0 : Integer.parseInt(v.replace(",", ""));

                String key = currentGu + "|" + region + "|" + years.get(y);
                Integer prev = merged.put(key, cnt);
                if (prev != null) {
                    throw new IllegalStateException("같은 연도에 값이 중복됨: " + key + " (" + prev + " vs " + cnt + ")");
                }
            }
        }

        List<Map<String, Object>> out = new ArrayList<>();
        for (Map.Entry<String, Integer> e : merged.entrySet()) {
            String[] k = e.getKey().split("\\|");
            out.add(Map.of("gu", k[0], "dong", k[1], "year", k[2], "cnt", e.getValue()));
        }

        Path output = Path.of("data", "fire.json");
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(output.toFile(), out);
        System.out.printf("추출 완료: %d건 -> %s%n", out.size(), output);
    }

    /** Cell의 ss:Index(1-based)로 건너뛴 열을 보정해 셀 값 목록을 만든다 */
    private static List<String> readCells(Element row) {
        List<String> cells = new ArrayList<>();
        NodeList children = row.getElementsByTagName("Cell");
        for (int i = 0; i < children.getLength(); i++) {
            Element cell = (Element) children.item(i);
            String idx = cell.getAttribute("ss:Index");
            if (!idx.isEmpty()) {
                while (cells.size() < Integer.parseInt(idx) - 1) cells.add(null);
            }
            NodeList data = cell.getElementsByTagName("Data");
            cells.add(data.getLength() == 0 ? null : data.item(0).getTextContent().trim());
        }
        return cells;
    }
}
