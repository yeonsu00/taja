package com.taja.application.weather;

import com.taja.global.exception.ReadFileException;
import com.taja.domain.weather.DistrictPoint;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistrictPointFileReader {

    private static final String SHEET_NAME = "district point";

    public List<DistrictPoint> readDistrictPointFromFile(MultipartFile file) {
        try (Workbook workbook = openWorkbook(file)) {
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            DataFormatter formatter = new DataFormatter();

            List<DistrictPoint> districtPoints = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                DistrictPoint districtPoint = createDistrictPointFromRow(row, formatter);

                if (districtPoint != null) {
                    districtPoints.add(districtPoint);
                }
            }

            return districtPoints;

        } catch (IOException e) {
            log.error("파일을 읽는 중 I/O 오류가 발생했습니다.", e);
            throw new ReadFileException("파일을 읽는 중 오류가 발생했습니다.");
        } catch (RuntimeException e) {
            log.error("엑셀 파일 처리 중 런타임 오류가 발생했습니다.", e);
            throw new ReadFileException("엑셀 파일 처리 중 오류가 발생했습니다.");
        }
    }

    private Workbook openWorkbook(MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        return new XSSFWorkbook(inputStream);
    }

    private DistrictPoint createDistrictPointFromRow(Row row, DataFormatter formatter) {
        try {
            String cityName = formatter.formatCellValue(row.getCell(2)).trim();
            String districtName = formatter.formatCellValue(row.getCell(3)).trim();
            String dongName = formatter.formatCellValue(row.getCell(4)).trim();

            if (cityName.equals("서울특별시") && dongName.isEmpty()) {
                Integer xPoint = Integer.parseInt(formatter.formatCellValue(row.getCell(5)));
                Integer yPoint = Integer.parseInt(formatter.formatCellValue(row.getCell(6)));

                return DistrictPoint.of(districtName, xPoint, yPoint);
            }
            return null;
        } catch (NumberFormatException e) {
            throw new ReadFileException("엑셀 파일의 데이터 형식이 올바르지 않습니다.");
        }
    }

}
