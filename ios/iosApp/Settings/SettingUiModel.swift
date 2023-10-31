//
//  SettingList.swift
//  iosApp
//
//  Created by Ashish Ekka on 31/10/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation

struct SettingUiModel: Identifiable {
    let id = UUID()
    let key: Key
    let kind: Kind
    var boolVal: Bool = false
    var stringVal: String = ""
    var options: [Option] = []
}

struct Option: Identifiable {
    let id = UUID()
    var name: String
    var isSelected: Bool = false
}

extension SettingUiModel {
    enum Key: String, CaseIterable {
        case sound
        case haptics
        case theme
        case speed
        case about
    }
}

extension SettingUiModel {
    enum Kind {
        case text(config: TextConfig)
        case toggle(config: ToggleConfig)
        case picker(config: PickerConfig)
    }
}

extension SettingUiModel.Kind {
    struct TextConfig {
        let title: String
        let description: String
    }
    
    struct ToggleConfig {
        let title: String
        let description: String
    }
    
    struct PickerConfig {
        let title: String
        let description: String
    }
}
