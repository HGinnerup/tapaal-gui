package pipe.gui.graphicElements;

import net.tapaal.gui.DrawingSurfaceManager.AbstractDrawingSurfaceManager;
import net.tapaal.helpers.Reference.Reference;
import pipe.gui.Translatable;
import pipe.gui.Zoomable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public abstract class GraphicalElement extends JComponent implements Zoomable, Translatable {


    protected Reference<AbstractDrawingSurfaceManager> managerRef = null;

    /*private*/ public GraphicalElement(){
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (getManagerRef() !=null && getManagerRef().get()!=null) {
                    getManagerRef().get().triggerEvent(new AbstractDrawingSurfaceManager.DrawingSurfaceEvent(
                        GraphicalElement.this, e, AbstractDrawingSurfaceManager.MouseAction.clicked
                    ));
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (getManagerRef() !=null && getManagerRef().get()!=null) {
                    getManagerRef().get().triggerEvent(new AbstractDrawingSurfaceManager.DrawingSurfaceEvent(
                        GraphicalElement.this, e, AbstractDrawingSurfaceManager.MouseAction.pressed
                    ));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (getManagerRef() !=null && getManagerRef().get()!=null) {
                    getManagerRef().get().triggerEvent(new AbstractDrawingSurfaceManager.DrawingSurfaceEvent(
                        GraphicalElement.this, e, AbstractDrawingSurfaceManager.MouseAction.released
                    ));
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (getManagerRef() !=null && getManagerRef().get()!=null) {
                    getManagerRef().get().triggerEvent(new AbstractDrawingSurfaceManager.DrawingSurfaceEvent(
                        GraphicalElement.this, e, AbstractDrawingSurfaceManager.MouseAction.entered
                    ));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (getManagerRef() !=null && getManagerRef().get()!=null) {
                    getManagerRef().get().triggerEvent(new AbstractDrawingSurfaceManager.DrawingSurfaceEvent(
                        GraphicalElement.this, e, AbstractDrawingSurfaceManager.MouseAction.exited
                    ));
                }
            }
        });

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (getManagerRef() !=null && getManagerRef().get()!=null) {
                    getManagerRef().get().triggerEvent(new AbstractDrawingSurfaceManager.DrawingSurfaceEvent(
                        GraphicalElement.this, e, AbstractDrawingSurfaceManager.MouseAction.wheel
                    ));
                }
            }
        });

        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (getManagerRef() !=null && getManagerRef().get()!=null) {
                    getManagerRef().get().triggerEvent(new AbstractDrawingSurfaceManager.DrawingSurfaceEvent(
                        GraphicalElement.this, e, AbstractDrawingSurfaceManager.MouseAction.dragged
                    ));
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (getManagerRef()!=null && getManagerRef().get()!=null) {
                    getManagerRef().get().triggerEvent(new AbstractDrawingSurfaceManager.DrawingSurfaceEvent(
                        GraphicalElement.this, e, AbstractDrawingSurfaceManager.MouseAction.moved
                    ));
                }
            }
        });

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
    }

    public abstract int getLayerOffset();

    public Reference<AbstractDrawingSurfaceManager> getManagerRef() {
        return managerRef;
    }

    public void setManagerRef(Reference<AbstractDrawingSurfaceManager> manager) {
        this.managerRef = manager;
    }

    public abstract void addedToGui();

    public abstract void removedFromGui();

    /*public abstract static class GraphicalNode extends GraphicalElement implements Translatable{
        protected GraphicalNode() {
            super();

        }
    }

    public abstract static class GraphicalVertex extends GraphicalElement {
        protected GraphicalVertex() {
            super();

        }
    }*/

}
