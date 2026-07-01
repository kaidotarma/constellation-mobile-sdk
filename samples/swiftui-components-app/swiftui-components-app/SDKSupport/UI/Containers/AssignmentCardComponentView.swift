import ConstellationSdk
import SwiftUI

struct AssignmentCardComponentView: View {
    @ObservedObject var state: ObservableComponent<AssignmentCardComponent>

    init(_ component: AssignmentCardComponent) {
        state = ObservableComponent(component: component)
    }

    var body: some View {
        VStack {
            ScrollView {
                VStack(spacing: 20) {
                    VStack {
                        ForEach(state.component.children.filter { !($0 is ActionButtonsComponent) }, id: \.context.id) { child in
                            child.renderView()
                        }
                    }
                }
                // Style taken from OneColumnPage
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(20)
                .background(Color(red: 0.8, green: 0.85, blue: 0.9))
                .cornerRadius(10)
            }
            if let buttons = state.component.children.first(where: { $0 is ActionButtonsComponent }) as? ActionButtonsComponent {
                buttons.renderView()
            }
        }
    }
}
