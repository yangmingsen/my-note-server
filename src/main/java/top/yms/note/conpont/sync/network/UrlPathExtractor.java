package top.yms.note.conpont.sync.network;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * this code write by deepseek
 */
public class UrlPathExtractor {
    /**
     * 提取URL中的一级目录（保留原有方法）
     */
    public static String extractFirstLevelDirectory(String url) {
        return extractDirectoryByLevel(url, 1);
    }

    /**
     * 提取URL中的二级目录
     * @param url 输入的URL字符串
     * @return 二级目录名称，如果没有则返回空字符串
     */
    public static String extractSecondLevelDirectory(String url) {
        return extractDirectoryByLevel(url, 2);
    }

    /**
     * 提取URL中的三级目录
     * @param url 输入的URL字符串
     * @return 三级目录名称，如果没有则返回空字符串
     */
    public static String extractThirdLevelDirectory(String url) {
        return extractDirectoryByLevel(url, 3);
    }

    /**
     * 通用方法：提取指定层级的目录
     * @param url 输入的URL字符串
     * @param level 目录层级（1=一级目录，2=二级目录，3=三级目录...）
     * @return 指定层级的目录名称，如果没有则返回空字符串
     */
    public static String extractDirectoryByLevel(String url, int level) {
        if (level < 1) {
            return "";
        }

        try {
            // 解析URL
            URI uri = new URI(url);
            String path = uri.getPath();

            // 如果路径为空或为根路径，返回空字符串
            if (path == null || path.isEmpty() || path.equals("/")) {
                return "";
            }

            // 移除开头和结尾的斜杠，并分割路径
            String cleanPath = path.startsWith("/") ? path.substring(1) : path;

            // 按斜杠分割路径
            String[] pathSegments = cleanPath.split("/");

            // 检查请求的层级是否存在于路径中
            if (level > pathSegments.length) {
                return "";
            }

            // 获取指定层级的目录
            String targetSegment = pathSegments[level - 1];

            // 检查是否是文件（包含扩展名），如果是文件则视为目录名
            // 这里我们直接返回，不判断是否为文件
            return targetSegment;

        } catch (URISyntaxException e) {
            return "";
        }
    }

    /**
     * 提取二级目录（备用实现方法）
     * 这个方法会跳过文件名，只返回目录
     */
    public static String extractSecondLevelDirectoryOnly(String url) {
        return extractDirectoryOnlyByLevel(url, 2);
    }

    /**
     * 提取三级目录（备用实现方法）
     * 这个方法会跳过文件名，只返回目录
     */
    public static String extractThirdLevelDirectoryOnly(String url) {
        return extractDirectoryOnlyByLevel(url, 3);
    }

    /**
     * 提取指定层级的目录（跳过文件名）
     * @param url 输入的URL字符串
     * @param level 目录层级（1=一级目录，2=二级目录，3=三级目录...）
     * @return 指定层级的目录名称，如果是文件名则返回空字符串
     */
    public static String extractDirectoryOnlyByLevel(String url, int level) {
        if (level < 1) {
            return "";
        }

        try {
            // 解析URL
            URI uri = new URI(url);
            String path = uri.getPath();

            // 如果路径为空或为根路径，返回空字符串
            if (path == null || path.isEmpty() || path.equals("/")) {
                return "";
            }

            // 移除开头和结尾的斜杠，并分割路径
            String cleanPath = path.startsWith("/") ? path.substring(1) : path;

            // 按斜杠分割路径
            String[] pathSegments = cleanPath.split("/");

            // 检查请求的层级是否存在于路径中
            if (level > pathSegments.length) {
                return "";
            }

            // 获取指定层级的目录
            String targetSegment = pathSegments[level - 1];

            // 检查是否是文件（包含扩展名）
            // 简单的判断：如果包含"."且不是以"."开头（排除隐藏文件），可能是一个文件
            if (targetSegment.contains(".") && targetSegment.indexOf(".") > 0) {
                // 进一步判断是否看起来像文件扩展名
                String extension = targetSegment.substring(targetSegment.lastIndexOf(".") + 1);
                // 常见的文件扩展名列表
                String[] fileExtensions = {"html", "htm", "php", "asp", "jsp", "xml", "json",
                        "js", "css", "jpg", "png", "gif", "pdf", "txt"};
                for (String ext : fileExtensions) {
                    if (extension.equalsIgnoreCase(ext)) {
                        return "";
                    }
                }
            }

            return targetSegment;

        } catch (URISyntaxException e) {
            return "";
        }
    }

}
