//
//  KeyPad.swift
//  iosApp
//
//  Created by Ashish Ekka on 30/10/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct KeyPad: View {
    
    var keys = ["1", "2", "3", "C",
                "4", "5", "6", "D",
                "7", "8", "9", "E",
                "A", "0", "B", "F"
    ]
    
    var gridItemLayout = [GridItem(.flexible(), spacing: 0), GridItem(.flexible(), spacing: 0), GridItem(.flexible(), spacing: 0), GridItem(.flexible(), spacing: 0)]
    
    let onKeyDown: (Int) -> Void
    let onKeyUp: (Int) -> Void
    
    var body: some View {
        LazyVGrid(columns: gridItemLayout, spacing: 20) {
            ForEach(keys, id: \.self) { key in
                Button {
                    onKeyUp(Int(key) ?? 16)
                } label: {
                    Text(key).font(.system(size: 24)).padding(16)
                }
                .buttonStyle(BorderedButtonStyle())
                .onLongPressGesture(minimumDuration: 0, perform: {}) { pressed in
                    if (pressed) {
                        onKeyDown(Int(key) ?? 16)
                    }
                }
            }
        }
    }
}

#Preview {
    KeyPad {_ in
        
    } onKeyUp: {_ in
        
    }
}
