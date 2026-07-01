import { ContainerBaseComponent } from "./container-base.component.js";

const ACTION_BUTTONS_TYPE = "ActionButtons";

export class AssignmentCardComponent extends ContainerBaseComponent {
    childrenPConns;
    arMainButtons$;
    arSecondaryButtons$;
    actionButtonClick;
    actionButtonsComponent;

    constructor(componentsManager, pConn, childrenPConns, mainButtons, secondaryButtons, actionButtonClick) {
        super(componentsManager, pConn);
        this.type = "AssignmentCard";
        this.childrenPConns = childrenPConns;
        this.arMainButtons$ = mainButtons;
        this.arSecondaryButtons$ = secondaryButtons;
        this.actionButtonClick = actionButtonClick;
    }

    init() {
        this.componentsManager.onComponentAdded(this);
        this.reconcileChildren(this.childrenPConns);

        this.actionButtonsComponent = this.componentsManager.create(ACTION_BUTTONS_TYPE, [
            this.arMainButtons$,
            this.arSecondaryButtons$,
            this.actionButtonClick,
        ]);
        this.actionButtonsComponent.init();
        this.#sendPropsUpdate();
    }

    destroy() {
        this.actionButtonsComponent?.destroy();
        this.actionButtonsComponent = undefined;
        super.destroy();
    }

    update(pConn, pConnChildren, mainButtons, secondaryButtons, actionButtonClick) {
        this.pConn = pConn;
        this.childrenPConns = pConnChildren;
        this.arMainButtons$ = mainButtons;
        this.arSecondaryButtons$ = secondaryButtons;
        this.actionButtonClick = actionButtonClick;

        this.reconcileChildren(this.childrenPConns);
        this.actionButtonsComponent?.update(this.arMainButtons$, this.arSecondaryButtons$, this.actionButtonClick);
        this.#sendPropsUpdate();
    }

    #sendPropsUpdate() {
        let children;
        if (this.actionButtonsComponent) {
            children = [
                ...this.getChildrenProps(),
                { id: this.actionButtonsComponent?.compId, type: ACTION_BUTTONS_TYPE }
            ]
        } else {
            children = this.getChildrenProps();
        }
        this.props = {
            children: children
        };
        this.componentsManager.onComponentPropsUpdate(this);
    }
}
