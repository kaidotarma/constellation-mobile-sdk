import ConstellationSdk
import SwiftUI

struct FlowContainerComponentView: View {
    @ObservedObject var state: ObservableComponent<FlowContainerComponent>

    init(_ component: FlowContainerComponent) {
        state = ObservableComponent(component: component)
    }

    var body: some View {
        VStack {
            if !state.component.title.isEmpty {
                Text(state.component.title)
                    .font(.title)
            }
            ForEach(state.component.children.filter { $0 is AlertBannerComponent }, id: \.context.id) { child in
                child.renderView()
                    .cornerRadius(10)
            }
            ForEach(state.component.children.filter { $0 is AssignmentComponent }, id: \.context.id) { child in
                child.renderView()
                    .cornerRadius(10)
            }
        }
    }
}
