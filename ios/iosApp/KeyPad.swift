//
//  KeyPad.swift
//  iosApp
//
//  Created by Ashish Ekka on 30/10/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct KeyPad: View {
    
    private var keys = ["1", "2", "3", "C",
                        "4", "5", "6", "D",
                        "7", "8", "9", "E",
                        "A", "0", "B", "F"
    ]
    
    private var gridItemLayout = [GridItem(.flexible(), spacing: 0), GridItem(.flexible(), spacing: 0), GridItem(.flexible(), spacing: 0), GridItem(.flexible(), spacing: 0)]
    
    var body: some View {
        LazyVGrid(columns: gridItemLayout, spacing: 20) {
            ForEach(keys, id: \.self) { key in
                Button {
                    print("Clicked \(key)")
                } label: {
                    Text(key).font(.system(size: 24)).padding(16)
                }.buttonStyle(BorderedButtonStyle())
            }
        }
    }
}

#Preview {
    KeyPad()
}
