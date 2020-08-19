<%@tag description="Main Body Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<div id="roiModal" class="modal modal-fixed-footer">
    <div class="modal-content">
        <h4>Roi Selection</h4>
        <p>Select pages on which the RoI should apply</p>
        <ul class="collapsible" data-collapsible="expandable">
             <li>
                 <div class="collapsible-header active"><i class="material-icons">settings</i>Options</div>
                    <div class="collapsible-body collapsible-body-batch">
                        <ul id="roi-settings">
                            <li class="select-all">
                                <input type="checkbox" class="" checked="checked" id="selectAllBatch"/>
                                <label for="selectRecto">
                                    Select all
                                </label>
                            </li>
                            <li class="select-recto">
                                <input type="checkbox" class="" id="selectRecto"/>
                                <label for="selectAllBatch">
                                    Recto
                                </label>
                            </li>
                            <li class="select-verso">
                                <input type="checkbox" class="" id="selectVerso"/>
                                <label for="selectVerso">
                                    Verso
                                </label>
                            </li>
                        </ul>
                    </div>
            </li>
            <li>
                <div class="collapsible-header"><i class="material-icons">library_books</i>Pages</div>
                <div class="collapsible-body collapsible-body-batch">
                    <ul id="batchImageList">
                        <c:forEach items="${book.getPages()}" var="bookpage">
                            <li>
                                <input type="checkbox" id="${bookpage.getName()}" data-page="${bookpage.getId()}"
                                       class="batchPageCheck"/>
                                <label for="${bookpage.getName()}">
                                        ${bookpage.getName()}
                                </label>
                            </li>
                        </c:forEach>
                    </ul>
                </div>
            </li>
        </ul>

    </div>
    <div class="modal-footer">
        <a href="#!" class="modal-close waves-effect waves-green btn-flat">Close</a>
        <a class="col s12 waves-effect waves-light btn confirmBatchSegment tooltipped"
           href="#roiConfirmationModal" data-position="left" data-delay="50"
           data-tooltip="Apply RoI selection">Next</a>
    </div>
    <!-- TODO: Rectangle Drawing after modal -->
</div>