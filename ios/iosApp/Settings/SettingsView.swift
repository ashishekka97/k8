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
                        case .toggle(let config):
                            Toggle(config.title, isOn: $setting.boolVal)
                            Text(config.description).font(.caption)
                        case .picker(let config):
                            Picker(config.title, selection: $setting.stringVal) {
                                ForEach($setting.options) { $option in
                                    Text($option.wrappedValue.name)
                                }
                            }
                            Text(config.description).font(.caption)
                        }
                    }
                }
            }.onAppear(perform: {
                viewModel.load()
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
