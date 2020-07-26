<%@tag description="Main Body Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<div id="metadataModal" class="modal modal-fixed-footer">
    <div class="modal-content container">
        <h4>Metadata</h4>
        <p>View and edit metadata</p>
        <div class="valign-wrapper">
            <table class="striped">
                <tbody>
                <tr>
                    <td>Creator</td>
                    <td class="input-field">
                        <input id="creator" class="metadata" type="text">
                    </td>
                    <td>
                        <a class="waves-effect waves-light btn setSessionUser tooltipped" data-position="left"
                           data-delay="50" data-tooltip="Set the current user for the whole session.">Set</a>
                    </td>
                </tr>
                <tr>
                    <td>Comments</td>
                    <td class="input-field">
                        <textarea id="comments" class="metadata materialize-textarea"></textarea>
                    </td>
                    <td></td>
                </tr>
                <tr>
                    <td>Created</td>
                    <td class="input-field">
                        <input id="creation-time" disabled class="metadata" type="text">
                    </td>
                    <td></td>
                </tr>
                <tr>
                    <td>Last change</td>
                    <td class="input-field">
                        <input id="last-modification-time" disabled class="metadata" type="text">
                    </td>
                    <td></td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
    <div class="modal-footer">
        <a href="#!" class="modal-close waves-effect waves-green btn-flat">Close</a>
    </div>
</div>
