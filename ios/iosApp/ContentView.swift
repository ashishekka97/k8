import SwiftUI
import common

struct ContentView: View {
    @StateObject var viewModel = MainViewModel(chip8: Chip8Impl())
	var body: some View {
        Canvas(rendersAsynchronously: false) { context, size in
            let gridTileWidth: CGFloat = size.width / CGFloat(64)
            let gridTileHeight: CGFloat = size.height / CGFloat(32)
            
            viewModel.vRam.indices.forEach { rowIndex in
                viewModel.vRam[rowIndex].indices.forEach { colIndex in
                    let xx = CGFloat(colIndex) * gridTileWidth
                    let yy = CGFloat(rowIndex) * gridTileHeight
                    let color = if(viewModel.vRam[rowIndex][colIndex]) {
                        Color.white
                    }
                    else {
                        Color.black
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
        .aspectRatio(CGFloat(64) / CGFloat(32), contentMode: .fit)
        .padding()
        .task {
            viewModel.loadAndStart()
            await viewModel.startObservingVRam()
        }
	}
}
