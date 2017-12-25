<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
</head>
<body>

<c:if test="${persons ne null}">
    <c:forEach var="person" items="${persons}">
        <div class="image-block" data-image="${person}">
            <img src="/person/image?name=${person}">
            <button class="delete">Delete</button>
        </div>
    </c:forEach>
</c:if>

<script src="/resources/js/jquery-3.1.0.min.js"></script>
<script src="/resources/js/ajax.js"></script>
<script>
    $('.image-block').each(function(){
        var $block = $(this);

        $block.find('.delete').click(function(){
            Ajax.delete({
                url: '/person/image?name=' + encodeURIComponent($block.data('image')),
                success: function(response) {
                    if (response) {
                        $block.remove();
                    } else {
                        alert('failed to delete');
                    }
                }
            })
        });
    });
</script>
</body>
</html>