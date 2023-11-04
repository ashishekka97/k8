//
//  SettingsViewModel.swift
//  iosApp
//
//  Created by Ashish Ekka on 31/10/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import common
import KMPNativeCoroutinesAsync

@MainActor
class SettingsViewModel : ObservableObject {
    @Published var settings: [SettingUiModel]
    private var k8Settings: K8Settings
    
    init(settings: K8Settings) {
        k8Settings = settings
        self.settings = []
    }
    
    func onToggleChange(key: SettingUiModel.Key, isEnabled: Bool) {
        _ = Task {
            do {
                switch (key) {
                case .sound: do {
                    _ = try await asyncFunction(for: k8Settings.setBooleanSetting(key: key.rawValue, value: isEnabled))
                }
                case .haptics: do {
                    _ = try await asyncFunction(for: k8Settings.setBooleanSetting(key: key.rawValue, value: isEnabled))
                }
                default: do {
                    
                }
                }
            } catch {
                print("Failed to set settings for \(key) to \(isEnabled)")
            }
        }
    }
    
    func onOptionSelected(key: SettingUiModel.Key, selection: Option) {
        _ = Task {
            do {
                switch (key) {
                case .theme: do {
                    let themeIndex = Int32(ThemeColor.companion.getThemeFromKey(key: selection.name).ordinal)
                    _ = try await asyncFunction(for: k8Settings.setIntSetting(key: key.rawValue, value: themeIndex))
                }
            
                case .speed: do {
                    let speedIndex = Int32(EmulatorSpeed.companion.getSpeedFromKey(key: Float(selection.name) ?? 1.0).ordinal)
                    _ = try await asyncFunction(for: k8Settings.setIntSetting(key: key.rawValue, value: speedIndex))
                }
        
                default: do {
                    
                }
                }
            } catch {
                print("Failed to set settings for \(key) to \(String(describing: selection))")
            }
        }
    }
    
    func observeSettings() {
        let themeOptions = ThemeColor.companion.getAllThemes().map { scheme in Option(name: scheme)}
        let speedOptions = EmulatorSpeed.entries.map { speed in Option(name: "\(speed.speedFactor)")}
        let settingMap: KeyValuePairs<SettingUiModel.Key, SettingUiModel> = [
            .sound : SettingUiModel(key: .sound, kind: .toggle(config: .init(title: "Sound", description: "Enables sound emulation"), onToggled: onToggleChange(key:isEnabled:)), boolVal: false),
            .haptics: SettingUiModel(key: .haptics, kind: .toggle(config: .init(title: "Haptics", description: "Enables haptic feedback on key press"), onToggled: onToggleChange(key:isEnabled:)), boolVal: false),
            .theme: SettingUiModel(key: .theme, kind: .picker(config: .init(title: "Theme", description: "Changes theme for the screen"), onSelected: onOptionSelected(key:selection:)), options: themeOptions, optionVal: themeOptions[0]),
            .speed: SettingUiModel(key: .speed, kind: .picker(config: .init(title: "Speed", description: "Changes the CPU cycle speed"), onSelected: onOptionSelected(key:selection:)), options: speedOptions, optionVal: speedOptions[1]),
            .about: SettingUiModel(key: .about, kind: .text(config: .init(title: "About", description: "1.0")))
        ]
        
        _ = Task {
            do {
                let soundSequence = asyncSequence(for: k8Settings.getBooleanFlowSetting(key: SettingUiModel.Key.sound.rawValue))
                let hapticsSequence = asyncSequence(for: k8Settings.getBooleanFlowSetting(key: SettingUiModel.Key.haptics.rawValue))
                let themeSequence = asyncSequence(for: k8Settings.getIntFlowSetting(key: SettingUiModel.Key.theme.rawValue))
                let speedSequence = asyncSequence(for: k8Settings.getIntFlowSetting(key: SettingUiModel.Key.speed.rawValue))
                
                for try await (sound, haptics, theme, speed) in combineLatest(soundSequence, hapticsSequence, themeSequence, speedSequence) {
                    self.settings = settingMap.compactMap { (key, value) in
                        switch (key) {
                        case .sound: do {
                            var soundSetting = value
                            soundSetting.boolVal = sound.boolValue
                            return soundSetting
                        }
                        case .haptics: do {
                            var hapticsSetting = value
                            hapticsSetting.boolVal = haptics.boolValue
                            return hapticsSetting
                        }
                        case .theme: do {
                            var themeSetting = value
                            themeSetting.optionVal = themeOptions[theme.intValue]
                            return themeSetting
                        }
                        case .speed: do {
                            var speedSetting = value
                            speedSetting.optionVal = speedOptions[speed.intValue]
                            return speedSetting
                        }
                        default: do {
                            return value
                        }
                        }
                    }
                }
            } catch {
                print("failed with error")
            }
        }
    }
}
