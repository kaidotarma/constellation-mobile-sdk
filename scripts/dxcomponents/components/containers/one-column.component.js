import { ContainerBaseComponent } from "./container-base.component.js";

export class OneColumnComponent extends ContainerBaseComponent {

    constructor(componentsManager, pConn) {
        super(componentsManager, pConn);
        this.type = "OneColumn";
    }

    init() {
        this.componentsManager.onComponentAdded(this);
        this.reconcileChildren();
        this.sendPropsUpdate();
    }

    destroy() {
        // prevents sending fields from previous steps on next submit see: TASK-1776419 pulse
        PCore.getContextTreeManager().removeContextTreeNode(this.pConn.getContextName());
        super.destroy();
    }

    update(pConn) {
        this.pConn = pConn;
        this.reconcileChildren();
        this.sendPropsUpdate();
    }

    sendPropsUpdate() {
        this.props = {
            children: this.getChildrenProps(),
        };
        this.componentsManager.onComponentPropsUpdate(this);
    }
}
