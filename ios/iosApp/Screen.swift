//
//  Screen.swift
//  iosApp
//
//  Created by Ashish Ekka on 24/10/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI

struct Screen: View {
    @State var screenState: ScreenState
    var body: some View {
        Canvas(rendersAsynchronously: true) { context, size in
            let gridTileWidth: CGFloat = size.width / CGFloat(screenState.state[0].count)
            let gridTileHeight: CGFloat = size.height / CGFloat(screenState.state.count)
            
            screenState.state.indices.forEach { rowIndex in
                screenState.state[rowIndex].indices.forEach { colIndex in
                    let xx = CGFloat(colIndex) * gridTileWidth
                    let yy = CGFloat(rowIndex) * gridTileHeight
                    let color = if(screenState.state[rowIndex][colIndex]) {
                        screenState.foregroundColor
                    } else {
                        screenState.backgroundColor
                    }
                    context.fill(
                        Path(
                            CGRect(
                                x: xx,
                                y: yy,
                                width: gridTileWidth,
                                height: gridTileHeight
                            )
                        )
                        , with: .color(color)
                    )
                }
            }
        }
        .aspectRatio(CGFloat(screenState.state[0].count) / CGFloat(screenState.state.count), contentMode: .fit)
        .padding()
    }
}

#Preview {
    Screen(screenState: ScreenState(state: [[Bool]](repeating: [Bool](repeating: Bool.random(), count: 64), count: 32), foregroundColor: Color.white, backgroundColor: Color.black))
}
