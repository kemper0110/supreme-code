package net.danil.web.user.controller;

//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/user")
//public class UserController {
//    private final UserRepository userRepository;
//    private final ProjectionFactory pf = new SpelAwareProxyProjectionFactory();
//
//    @GetMapping
//    List<BasicUserProjection> index() {
//        return userRepository.findAllPreview();
//    }
//
//    @GetMapping("/{id}")
//    BasicUserProjection view(@PathVariable Long id) {
//        return userRepository.findById(id).map(u -> pf.createProjection(BasicUserProjection.class, u)).get();
//    }
//
//    @PostMapping
//    BasicUserProjection store(User user) {
//        return pf.createProjection(BasicUserProjection.class, userRepository.save(user));
//    }
//
//    @PutMapping("/{id}")
//    BasicUserProjection update(@PathVariable Long id, User user) {
//        return pf.createProjection(BasicUserProjection.class, userRepository.save(user));
//    }
//
//    @DeleteMapping("/{id}")
//    void delete(@PathVariable Long id) {
//        userRepository.deleteById(id);
//    }
//}
