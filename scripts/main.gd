extends Node3D

var cam_yaw = 0.0
var cam_pitch = -0.3
var cam_distance = 8.0
var cam_target = Vector3(0, 1, 0)
var player: CharacterBody3D
var anim_player: AnimationPlayer
var companion: CharacterBody3D
var move_dir = Vector2.ZERO
var speed = 4.0
var joy_origin = Vector2(115, 0)

func _ready():
	var screen_size = get_viewport().get_visible_rect().size
	joy_origin = Vector2(115, screen_size.y - 125)

	# Chão
	var ground = MeshInstance3D.new()
	var plane = PlaneMesh.new()
	plane.size = Vector2(200, 200)
	ground.mesh = plane
	var ground_mat = StandardMaterial3D.new()
	ground_mat.albedo_color = Color(0.29, 0.49, 0.24)
	ground.material_override = ground_mat
	add_child(ground)

	var ground_body = StaticBody3D.new()
	var ground_shape = CollisionShape3D.new()
	var box_shape = BoxShape3D.new()
	box_shape.size = Vector3(200, 0.1, 200)
	ground_shape.shape = box_shape
	ground_body.add_child(ground_shape)
	add_child(ground_body)

	# Sol
	var sun = DirectionalLight3D.new()
	sun.rotation_degrees = Vector3(-45, -30, 0)
	sun.shadow_enabled = true
	sun.light_energy = 1.2
	add_child(sun)

	# Céu
	var env_node = WorldEnvironment.new()
	var env = Environment.new()
	env.background_mode = Environment.BG_SKY
	var sky = Sky.new()
	sky.sky_material = ProceduralSkyMaterial.new()
	env.sky = sky
	env.ambient_light_source = Environment.AMBIENT_SOURCE_SKY
	env_node.environment = env
	add_child(env_node)

	# Player (Remy)
	player = CharacterBody3D.new()
	var col = CollisionShape3D.new()
	var cap = CapsuleShape3D.new()
	cap.radius = 0.3
	cap.height = 1.7
	col.shape = cap
	col.position = Vector3(0, 0.9, 0)
	player.add_child(col)
	add_child(player)

	var remy_scene = load("res://assets/characters/remy.fbx")
	var remy_visual = remy_scene.instantiate()
	player.add_child(remy_visual)
	anim_player = find_animation_player(remy_visual)
	if anim_player:
		anim_player.play("mixamo_com")

	# Animal companheiro (placeholder: cápsula + "martelo" caixa)
	companion = CharacterBody3D.new()
	companion.position = Vector3(2, 0, 2)
	var comp_mesh = MeshInstance3D.new()
	var comp_shape_mesh = CapsuleMesh.new()
	comp_shape_mesh.radius = 0.35
	comp_shape_mesh.height = 0.9
	comp_mesh.mesh = comp_shape_mesh
	comp_mesh.position = Vector3(0, 0.45, 0)
	var comp_mat = StandardMaterial3D.new()
	comp_mat.albedo_color = Color(0.4, 0.3, 0.2)
	comp_mesh.material_override = comp_mat
	companion.add_child(comp_mesh)

	var hammer = MeshInstance3D.new()
	var hammer_mesh = BoxMesh.new()
	hammer_mesh.size = Vector3(0.15, 0.4, 0.15)
	hammer.mesh = hammer_mesh
	hammer.position = Vector3(0.4, 0.6, 0)
	var hammer_mat = StandardMaterial3D.new()
	hammer_mat.albedo_color = Color(0.5, 0.5, 0.55)
	hammer.material_override = hammer_mat
	companion.add_child(hammer)

	var comp_col = CollisionShape3D.new()
	var comp_cap = CapsuleShape3D.new()
	comp_cap.radius = 0.35
	comp_cap.height = 0.9
	comp_col.shape = comp_cap
	comp_col.position = Vector3(0, 0.45, 0)
	companion.add_child(comp_col)
	add_child(companion)

	# Câmara livre
	var cam = Camera3D.new()
	cam.name = "FreeCamera"
	add_child(cam)
	update_camera(cam)

func find_animation_player(node: Node) -> AnimationPlayer:
	if node is AnimationPlayer:
		return node
	for child in node.get_children():
		var result = find_animation_player(child)
		if result:
			return result
	return null

func update_camera(cam: Camera3D):
	cam_target = player.position + Vector3(0, 1, 0)
	var offset = Vector3(
		cos(cam_pitch) * sin(cam_yaw),
		sin(cam_pitch),
		cos(cam_pitch) * cos(cam_yaw)
	) * cam_distance
	cam.position = cam_target + offset
	cam.look_at(cam_target, Vector3.UP)

func _physics_process(delta):
	if move_dir.length() > 0.15:
		var forward = Vector3(sin(cam_yaw), 0, cos(cam_yaw))
		var right = Vector3(cos(cam_yaw), 0, -sin(cam_yaw))
		var dir3 = (forward * -move_dir.y + right * move_dir.x).normalized()
		player.velocity = dir3 * speed
		player.velocity.y = -1.0
		player.look_at(player.position + dir3, Vector3.UP)
	else:
		player.velocity = Vector3(0, -1.0, 0)
	player.move_and_slide()

	# Companheiro segue o jogador a uma certa distância
	if companion:
		var to_player = player.position - companion.position
		to_player.y = 0
		var dist = to_player.length()
		if dist > 2.0:
			var dir = to_player.normalized()
			companion.velocity = dir * (speed * 0.9)
			companion.velocity.y = -1.0
			companion.look_at(companion.position + dir, Vector3.UP)
		else:
			companion.velocity = Vector3(0, -1.0, 0)
		companion.move_and_slide()

	var cam = get_node_or_null("FreeCamera")
	if cam:
		update_camera(cam)

func _input(event):
	var screen_size = get_viewport().get_visible_rect().size
	if event is InputEventScreenTouch:
		if not event.pressed and event.position.x < screen_size.x * 0.4:
			move_dir = Vector2.ZERO
	elif event is InputEventScreenDrag:
		if event.position.x < screen_size.x * 0.4:
			move_dir = (event.position - joy_origin) / 75.0
			move_dir = move_dir.limit_length(1.0)
		else:
			cam_yaw -= event.relative.x * 0.01
			cam_pitch = clamp(cam_pitch - event.relative.y * 0.01, -1.4, 1.4)
	elif event is InputEventMagnifyGesture:
		cam_distance = clamp(cam_distance / event.factor, 2.0, 40.0)
