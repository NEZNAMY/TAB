package me.neznamy.tab.shared.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for dumping data for debugging purposes.
 */
public class DumpUtils {

    /**
     * Converts a table represented by header and rows into a list of formatted strings.
     *
     * @param   header
     *          List of header column names
     * @param   rows
     *          List of rows, each row being a list of column values
     * @return  List of formatted strings representing the table
     */
    @NotNull
    public static List<String> tableToLines(@NotNull List<String> header, @NotNull List<List<String>> rows) {
        int cols = header.size();
        int[] widths = new int[cols];

        // 1. Compute column widths
        for (int i = 0; i < cols; i++) {
            widths[i] = header.get(i).length();
            for (List<String> row : rows) {
                widths[i] = Math.max(widths[i], row.get(i).length());
            }
        }

        List<String> result = new ArrayList<>();

        // 2. Header
        result.add(buildRow(header, widths));

        // 3. Separator
        result.add(buildSeparator(widths));

        // 4. Rows
        for (List<String> row : rows) {
            result.add(buildRow(row, widths));
        }

        return result;
    }

    @NotNull
    private static String buildRow(@NotNull List<String> row, int @NotNull [] widths) {
        StringBuilder sb = new StringBuilder();
        sb.append("|");
        for (int i = 0; i < row.size(); i++) {
            sb.append(" ");
            sb.append(row.get(i));
            sb.append(repeat(' ', widths[i] - row.get(i).length()));
            sb.append(" |");
        }
        return sb.toString();
    }

    @NotNull
    private static String buildSeparator(int @NotNull [] widths) {
        StringBuilder sb = new StringBuilder();
        sb.append("|");
        for (int w : widths) {
            sb.append(repeat('-', w + 2));
            sb.append("|");
        }
        return sb.toString();
    }

    @NotNull
    private static String repeat(char c, int count) {
        char[] arr = new char[count];
        Arrays.fill(arr, c);
        return new String(arr);
    }
}
