package com.company.app.setting;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SiteSettingService {

    private final SiteSettingRepository repository;

    @PostConstruct
    @Transactional
    public void initDefaults() {
        if (repository.findBySettingKey("show_prices").isEmpty()) {
            repository.save(SiteSetting.builder()
                    .settingKey("show_prices")
                    .settingValue("false")
                    .description("Controls whether product prices are visible on the public storefront or replaced with 'Price on Request'.")
                    .build());
            log.info("Initialized default site setting: show_prices = false");
        }
        if (repository.findBySettingKey("b2b_mode").isEmpty()) {
            repository.save(SiteSetting.builder()
                    .settingKey("b2b_mode")
                    .settingValue("true")
                    .description("Enable B2B catalog request-for-quote (RFQ) workflow mode.")
                    .build());
        }
    }

    public Map<String, Object> getPublicSettings() {
        Map<String, Object> publicMap = new HashMap<>();
        
        boolean showPrices = repository.findBySettingKey("show_prices")
                .map(s -> Boolean.parseBoolean(s.getSettingValue()))
                .orElse(false);

        boolean b2bMode = repository.findBySettingKey("b2b_mode")
                .map(s -> Boolean.parseBoolean(s.getSettingValue()))
                .orElse(true);

        publicMap.put("showPrices", showPrices);
        publicMap.put("b2bMode", b2bMode);
        publicMap.put("siteName", "Nandhas Engineering Works");
        publicMap.put("currency", "INR");

        return publicMap;
    }

    public List<SiteSetting> getAllSettings() {
        return repository.findAll();
    }

    @Transactional
    public SiteSetting updateSetting(String key, String value) {
        SiteSetting setting = repository.findBySettingKey(key)
                .orElseGet(() -> SiteSetting.builder().settingKey(key).build());

        setting.setSettingValue(value);
        return repository.save(setting);
    }

    @Transactional
    public Map<String, Object> updateSettingsMap(Map<String, Object> settings) {
        if (settings != null) {
            settings.forEach((k, v) -> {
                if (v != null) {
                    updateSetting(k, String.valueOf(v));
                }
            });
        }
        return getPublicSettings();
    }
}
