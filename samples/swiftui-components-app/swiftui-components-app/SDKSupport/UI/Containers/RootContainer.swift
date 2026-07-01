import ConstellationSdk
import SwiftUI

struct RootContainer: View {
    @ObservedObject var state: ObservableComponent<RootContainerComponent>
    
    init(_ component: RootContainerComponent) {
        state = ObservableComponent(component: component)
    }

    var body: some View {
        if let container = state.component.children.first(where: { $0 is ViewContainerComponent }) as? ViewContainerComponent {
            container.renderView()
                .dialog(
                    config: {
                        state.component.dialogConfig
                    },
                    onDismiss: {
                        state.component.dismissDialog()
                    }
                )
        }
    }
}
