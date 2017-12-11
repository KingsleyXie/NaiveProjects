<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/sql" prefix="sql"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no">
	<title>JSP DB List</title>
	<link rel="stylesheet" href="https://cdn.bootcss.com/bootstrap/3.3.7/css/bootstrap.min.css">
	<link rel="stylesheet" href="https://cdn.bootcss.com/font-awesome/4.7.0/css/font-awesome.css">
	<style>
		.panel-default > .panel-heading {
			height: 3em;
			background-color: #52acea;
		}
		.msg {
			font-size: 135%;
		}
		.msg-no {
			padding-right: 0.6em;
		}
		@media (max-width: 500px) {
			.msg-heading {
				height: 4.3em !important;
			}
		}
		@media (max-width: 500px) {
			.msg-time {
				clear: both;
			}
		}
	</style>
</head>

<body>
	<nav class="navbar navbar-default">
		<div class="container-fluid">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#menu">
					<span class="sr-only"></span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
				</button>
				<span class="navbar-brand">留言板</span>
			</div>

			<div class="collapse navbar-collapse" id="menu">
				<ul class="nav navbar-nav navbar-right">
					<li><a href ="#" data-toggle="modal" data-target="#leave-msg-modal">留下足迹</a></li>
				</ul>
			</div>
		</div>
	</nav>

	<div class="modal fade" tabindex="-1" id="leave-msg-modal">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">&times;</button>
					<h3 class="modal-title">发表留言</h3>
				</div>

				<form class="form-horizontal" method="POST" action="insert.jsp">
					<div class="modal-body">
						<div class="col-md-12 form-group">
							<label>昵称</label>
							<input type="text" class="form-control" name="nickname" placeholder="请输入姓名或昵称" required>
						</div>

						<div class="col-md-12 form-group">
							<label>留言内容</label>
							<textarea class="form-control" rows="3" name="message" placeholder="请输入留言内容" required></textarea>
						</div>
					</div>
					<div class="clearfix"></div>

					<div class="modal-footer">
						<input type="submit" class="btn btn-lg btn-primary btn-block" value="确定">
					</div>
				</form>
			</div>
		</div>
	</div>

	<c:import var="XMLfile" url="/WEB-INF/config.xml"/>
	<x:parse xml="${XMLfile}" var="configXML"/>

	<c:set var="url">jdbc:mysql://localhost:3306/<x:out select="$configXML/config/database"/>?useUnicode=true&characterEncoding=utf-8</c:set>
	<c:set var="username">
		<x:out select="$configXML/config/username"/>
	</c:set>
	<c:set var="password">
		<x:out select="$configXML/config/password"/>
	</c:set>

	<sql:setDataSource var="snapshot" driver="com.mysql.jdbc.Driver" url="${url}" user="${username}" password="${password}"/>
	<sql:query dataSource="${snapshot}" var="result">SELECT * FROM forum;</sql:query>

	<c:forEach items="${result.rows}" var="row">
	<div class="container">
		<div class="row clearfix">
			<div class="col-md-12 column">
				<div class="panel panel-default">
					<div class="panel-heading msg-heading">
						<div class="panel-title">
							<div class="pull-left msg-no">#<c:out value="${row.id}"/></div>
							<div class="pull-left"><c:out value="${row.nickname}"/></div>
							<div class="pull-right msg-time">
								<i class="fa fa-calendar"></i> <fmt:formatDate pattern="yyyy-MM-dd" value="${row.postTime}"/> <i class="fa fa-clock-o"></i> <fmt:formatDate pattern="HH:mm:ss" value="${row.postTime}"/>
							</div>
						</div>
					</div>

					<div class="row panel-body">
						<div class="col-md-9 msg"><c:out value="${row.message}"/></div>
					</div>
				</div>
			</div>
		</div>
	</div>
	</c:forEach>

	<script src="https://cdn.bootcss.com/jquery/3.2.1/jquery.min.js"></script>
	<script src="https://cdn.bootcss.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
</body>
</html>
