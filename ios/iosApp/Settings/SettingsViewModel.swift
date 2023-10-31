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
    
    init() {
        self.settings = []
        k8Settings = K8Settings()
    }
    
    
    func load() {
        self.settings = SettingUiModel.Key.allCases.map { key in
            switch (key) {
            case .sound: SettingUiModel(key: .sound, kind: .toggle(config: .init(title: "Sound", description: "Enables sound emulation")), boolVal: false)
            case .haptics: SettingUiModel(key: .haptics, kind: .toggle(config: .init(title: "Haptics", description: "Enables haptic feedback on key press")), boolVal: false)
            case .theme: SettingUiModel(key: .theme, kind: .picker(config: .init(title: "Theme", description: "Changes theme for the screen")), stringVal: "Gameboy", options: [Option(name: "Default"), Option(name: "Gameboy")])
            case .speed: SettingUiModel(key: .speed, kind: .picker(config: .init(title: "Speed", description: "Changes the CPU cycle speed")), stringVal: "1.0x", options: [Option(name: "0.5x"), Option(name: "1.0x")])
            case .about: SettingUiModel(key: .about, kind: .text(config: .init(title: "About", description: "1.0")))
            }
        }
    }
    
    func observeSettings() async {
        do {
            let sound = asyncSequence(for: k8Settings.getBooleanFlowSetting(key: "KEY_SOUND"))
            let haptics = asyncSequence(for: k8Settings.getBooleanFlowSetting(key: "KEY_HAPTICS"))
            let theme = asyncSequence(for: k8Settings.getIntFlowSetting(key: "KEY_THEME"))
            let speed = asyncSequence(for: k8Settings.getIntFlowSetting(key: "KEY_SPEED"))
            
            
            for try await data in sound {
                self.settings[0].boolVal = data.boolValue
            }
            for try await data in haptics {
                self.settings[1].boolVal = data.boolValue
            }
            for try await data in theme {
                self.settings[2].stringVal = self.settings[2].options[data.intValue].name
            }
            for try await data in speed {
                self.settings[3].stringVal = self.settings[3].options[data.intValue].name
            }
        } catch {
            print("failed with error")
        }
    }
}
