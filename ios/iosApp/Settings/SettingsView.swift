//
//  SettingsView.swift
//  iosApp
//
//  Created by Ashish Ekka on 30/10/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct SettingsView: View {
    
    @StateObject var viewModel = SettingsViewModel()
    
    let successFullyChanged: (_ data: [SettingUiModel]) -> Void
    
    var body: some View {
        NavigationView {
            Form {
                ForEach($viewModel.settings) { $setting in
                    VStack(alignment: .leading) {
                        switch(setting.kind) {
                        case .text(let config):
                            Text(config.title)
                            Text(config.description).font(.caption)
                        case .toggle(let config, let onToggled):
                            Toggle(config.title, isOn: $setting.boolVal)
                                .onChange(of: $setting.wrappedValue.boolVal) { value in
                                    onToggled($setting.wrappedValue.key, value)
                                }
                            Text(config.description).font(.caption)
                        case .picker(let config, let onSelected):
                            Picker(config.title, selection: $setting.optionVal) {
                                ForEach($setting.options) { $option in
                                    Text($option.wrappedValue.name).tag($option.wrappedValue as Option?)
                                }
                            }.onChange(of: $setting.wrappedValue.optionVal) { tag in
                                if (tag != nil) {
                                    onSelected($setting.wrappedValue.key, tag!)
                                }
                            }
                            Text(config.description).font(.caption)
                        }
                    }
                }
            }.onAppear(perform: {
                Task {
                    await viewModel.observeSettings()
                }
            })
            .navigationTitle("Settings")
            .toolbar(content: {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button {
                        successFullyChanged(viewModel.settings)
                    } label: {
                        Image(systemName: "arrow.uturn.backward")
                    }
                }
            })
        }
    }
}

#Preview {
    SettingsView { _ in }
}
