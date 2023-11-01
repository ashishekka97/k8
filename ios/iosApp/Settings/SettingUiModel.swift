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
    var optionVal: Option? = nil
}

struct Option: Identifiable {
    let id = UUID()
    var name: String
    var isSelected: Bool = false
}

extension Option : Hashable {
    static func == (lhs: Option, rhs: Option) -> Bool {
        return lhs.id == rhs.id && lhs.name == rhs.name && lhs.isSelected == rhs.isSelected
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
        hasher.combine(name)
        hasher.combine(isSelected)
    }
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
        case toggle(config: ToggleConfig, onToggled: (Key, Bool) -> Void)
        case picker(config: PickerConfig, onSelected: (Key, Option) -> Void)
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
