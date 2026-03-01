package top.yms.note.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.yms.note.conpont.crawler.CrawlerService;
import top.yms.note.entity.RestOut;
import top.yms.note.service.TempService;

import javax.annotation.Resource;

@RestController
@RequestMapping("/setting/")
public class SettingController {

    @Resource
    private CrawlerService crawlerService;

    @Resource
    private TempService tempService;

    @GetMapping("/doCrawler")
    public RestOut<String> doCrawler() {
        crawlerService.doCrawler();
        return RestOut.succeed("Ok");
    }

    @GetMapping("/networkResourceTransfer")
    public RestOut<String> networkResourceTransfer() {
        return tempService.networkResourceInfoFromMongoToMysql();
    }
}
